package com.forget.academy.service;

import com.forget.academy.common.CheckinTypes;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.CheckinPending;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingCancelRejectsPendingCheckinTest {

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
    void adminCancel_rejectsPendingClassCheckin() {
        Booking booking = new Booking();
        booking.setId(9L);
        booking.setUserId(100L);
        booking.setScheduleId(55L);
        booking.setClassDate("2026-09-08");
        booking.setStatus("待上课");
        booking.setBookingKey("group:2026-09-08:55");
        booking.setCardConsumed(false);
        when(bookingRepo.findById(9L)).thenReturn(Optional.of(booking));
        when(scheduleRepo.findById(55L)).thenReturn(Optional.empty());

        CheckinPending pending = new CheckinPending();
        pending.setId(3L);
        pending.setStatus(CheckinPendingService.STATUS_PENDING);
        pending.setCheckinType(CheckinTypes.CLASS);
        when(checkinPendingRepo.findByUserIdAndScheduleIdAndClassDateAndCheckinType(
                eq(100L), eq(55L), eq("2026-09-08"), eq(CheckinTypes.CLASS)))
                .thenReturn(Optional.of(pending));

        service.adminCancel(9L);

        assertEquals("已取消", booking.getStatus());
        assertTrue(booking.getBookingKey().contains(":x:"));
        ArgumentCaptor<CheckinPending> captor = ArgumentCaptor.forClass(CheckinPending.class);
        verify(checkinPendingRepo).save(captor.capture());
        assertEquals(CheckinPendingService.STATUS_REJECTED, captor.getValue().getStatus());
        assertEquals("系统", captor.getValue().getConfirmedByName());
    }
}
