package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.ClassSessionCancel;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.UserCard;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.ClassSessionCancelRepo;
import com.forget.academy.repo.ScheduleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceNewFeaturesTest {

    @Mock private ScheduleRepo scheduleRepo;
    @Mock private AdminAccessService adminAccessService;
    @Mock private BookingRepo bookingRepo;
    @Mock private AppUserRepo appUserRepo;
    @Mock private BookingRemindService bookingRemindService;
    @Mock private UserCardService userCardService;
    @Mock private UserCampusService userCampusService;
    @Mock private DanceCategoryService danceCategoryService;
    @Mock private ClassSessionCancelRepo classSessionCancelRepo;
    @Mock private CheckinPendingRepo checkinPendingRepo;

    private BookingService service;

    @BeforeEach
    void setUp() {
        service = new BookingService(
                scheduleRepo,
                adminAccessService,
                bookingRepo,
                appUserRepo,
                bookingRemindService,
                userCardService,
                userCampusService,
                danceCategoryService,
                classSessionCancelRepo,
                checkinPendingRepo
        );
    }

    @Test
    void listSchedules_showsClosedDoorToEveryone_butBlocksUnauthorizedBook() {
        Schedule closed = new Schedule();
        closed.setId(9L);
        closed.setType("group");
        closed.setName("闭门编舞");
        closed.setTimeText("20:00-21:00");
        closed.setClosedDoor(true);
        closed.setAudienceGroup("advanced");
        closed.setCapacity(20);
        closed.setEnabled(true);
        closed.setCampusId("1");

        AppUser normal = new AppUser();
        normal.setId(3L);
        normal.setClosedClassGroup(null);

        when(appUserRepo.findById(3L)).thenReturn(Optional.of(normal));
        when(scheduleRepo.findByTypeAndWeekdayAndCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(
                eq("group"), any(Integer.class), eq("1")))
                .thenReturn(List.of(closed));
        when(bookingRepo.countByScheduleIdAndClassDateAndStatusIn(eq(9L), eq("2026-09-20"), any()))
                .thenReturn(0L);
        when(bookingRepo.countByScheduleIdAndClassDateAndStatus(9L, "2026-09-20", "已完成"))
                .thenReturn(0L);
        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(9L, "2026-09-20")).thenReturn(false);
        when(bookingRepo.findByUserIdAndBookingKey(eq(3L), any())).thenReturn(Optional.empty());

        List<Map<String, Object>> rows = service.listSchedules("group", "2026-09-20", "1", 3L);

        assertEquals(1, rows.size());
        assertEquals(true, rows.get(0).get("closedDoor"));
        assertEquals(false, rows.get(0).get("closedDoorAccessible"));
        assertEquals(false, rows.get(0).get("canBook"));
        assertTrue(String.valueOf(rows.get(0).get("bookBlockReason")).contains("闭门"));
    }

    @Test
    void cancelSessionForLowEnrollment_setsSystemCancelSource() {
        Schedule schedule = new Schedule();
        schedule.setId(11L);

        Booking pending = new Booking();
        pending.setId(100L);
        pending.setStatus("待上课");
        pending.setBookingKey("group:2026-09-20:11");
        pending.setUserId(1L);
        pending.setScheduleId(11L);
        pending.setClassDate("2026-09-20");

        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(11L, "2026-09-20")).thenReturn(false);
        when(bookingRepo.countByScheduleIdAndClassDateAndStatus(11L, "2026-09-20", "已完成")).thenReturn(0L);
        when(bookingRepo.findByScheduleIdAndClassDateAndStatusOrderByIdAsc(11L, "2026-09-20", "待上课"))
                .thenReturn(List.of(pending));
        when(bookingRepo.findByScheduleIdAndClassDateAndStatusOrderByIdAsc(11L, "2026-09-20", "排队中"))
                .thenReturn(List.of());
        when(checkinPendingRepo.findByUserIdAndScheduleIdAndClassDateAndCheckinType(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        service.cancelSessionForLowEnrollment(schedule, "2026-09-20", 1, 4);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepo).save(captor.capture());
        assertEquals("已取消", captor.getValue().getStatus());
        assertEquals(BookingService.CANCEL_SOURCE_SYSTEM, captor.getValue().getCancelSource());
        verify(classSessionCancelRepo).save(any(ClassSessionCancel.class));
    }

    @Test
    void restoreSession_restoresSystemCancelledBookingsAndClearsCancelRecord() {
        Schedule schedule = new Schedule();
        schedule.setId(22L);
        schedule.setType("group");
        schedule.setCapacity(20);
        schedule.setCampusId("1");
        schedule.setSectionId(1L);

        ClassSessionCancel cancel = new ClassSessionCancel();
        cancel.setScheduleId(22L);
        cancel.setClassDate("2026-09-21");

        Booking systemCancelled = new Booking();
        systemCancelled.setId(200L);
        systemCancelled.setUserId(8L);
        systemCancelled.setScheduleId(22L);
        systemCancelled.setClassDate("2026-09-21");
        systemCancelled.setTab("group");
        systemCancelled.setStatus("已取消");
        systemCancelled.setCancelSource(BookingService.CANCEL_SOURCE_SYSTEM);
        systemCancelled.setBookingKey("group:2026-09-21:22:x:200");

        Booking userCancelled = new Booking();
        userCancelled.setId(201L);
        userCancelled.setUserId(9L);
        userCancelled.setScheduleId(22L);
        userCancelled.setClassDate("2026-09-21");
        userCancelled.setTab("group");
        userCancelled.setStatus("已取消");
        userCancelled.setCancelSource(BookingService.CANCEL_SOURCE_USER);
        userCancelled.setBookingKey("group:2026-09-21:22:x:201");

        AppUser user = new AppUser();
        user.setId(8L);
        UserCard card = new UserCard();
        card.setId(77L);

        when(scheduleRepo.findById(22L)).thenReturn(Optional.of(schedule));
        when(classSessionCancelRepo.findByScheduleIdAndClassDate(22L, "2026-09-21"))
                .thenReturn(Optional.of(cancel));
        when(bookingRepo.findByScheduleIdAndClassDateAndStatusOrderByIdAsc(22L, "2026-09-21", "已取消"))
                .thenReturn(List.of(systemCancelled, userCancelled));
        when(bookingRepo.countByScheduleIdAndClassDateAndStatusIn(eq(22L), eq("2026-09-21"), any()))
                .thenReturn(0L);
        when(appUserRepo.findById(8L)).thenReturn(Optional.of(user));
        when(userCardService.requireUsableGroupCard(8L, 1L)).thenReturn(card);

        Map<String, Object> result = service.restoreSession(22L, "2026-09-21");

        assertEquals(1, result.get("restoredPending"));
        assertEquals(1, result.get("skipped"));
        assertEquals(false, result.get("sessionCancelled"));
        verify(classSessionCancelRepo).delete(cancel);
        assertEquals("待上课", systemCancelled.getStatus());
        assertEquals("group:2026-09-21:22", systemCancelled.getBookingKey());
        assertEquals("已取消", userCancelled.getStatus());
    }

    @Test
    void restoreSession_throwsWhenNotCancelled() {
        Schedule schedule = new Schedule();
        schedule.setId(33L);
        schedule.setCampusId("1");
        when(scheduleRepo.findById(33L)).thenReturn(Optional.of(schedule));
        when(classSessionCancelRepo.findByScheduleIdAndClassDate(33L, "2026-09-22"))
                .thenReturn(Optional.empty());

        assertThrows(BizException.class, () -> service.restoreSession(33L, "2026-09-22"));
        verify(classSessionCancelRepo, never()).delete(any());
    }
}
