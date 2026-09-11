package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckinServiceMutableResultTest {

    @Mock private PracticeRecordRepo practiceRecordRepo;
    @Mock private ScheduleRepo scheduleRepo;
    @Mock private BookingRepo bookingRepo;
    @Mock private UserCampusService userCampusService;
    @Mock private UserCardService userCardService;

    private CheckinService service;

    @BeforeEach
    void setUp() {
        service = new CheckinService(
                practiceRecordRepo, scheduleRepo, bookingRepo, new ObjectMapper(), userCampusService, userCardService);
    }

    @Test
    void manualCheckin_returnsMutableMap_soCallerCanOverwriteMessage() {
        Schedule schedule = schedule(55L);
        when(scheduleRepo.findById(55L)).thenReturn(Optional.of(schedule));
        when(bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                eq(100L), eq(55L), eq("2026-09-08"), eq("待上课")))
                .thenReturn(Optional.of(new Booking()));
        when(practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(anyLong(), anyString(), anyString()))
                .thenReturn(false);
        when(practiceRecordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = service.manualCheckin(100L, 55L, "2026-09-08", "管理员", CheckinService.SOURCE_CONFIRMED);

        assertTrue((Boolean) result.get("ok"));
        assertDoesNotThrow(() -> result.put("message", "已确认到场"));
        assertEquals("已确认到场", result.get("message"));
        assertDoesNotThrow(() -> result.put("pendingId", 11L));
        assertEquals(11L, result.get("pendingId"));
    }

    @Test
    void manualCheckin_rejectsWhenBookingCancelled() {
        Schedule schedule = schedule(55L);
        when(scheduleRepo.findById(55L)).thenReturn(Optional.of(schedule));
        when(bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                eq(100L), eq(55L), eq("2026-09-08"), eq("待上课")))
                .thenReturn(Optional.empty());

        BizException ex = assertThrows(BizException.class,
                () -> service.manualCheckin(100L, 55L, "2026-09-08", "管理员"));
        assertEquals("预约已取消，无法签到", ex.getMessage());
        verify(practiceRecordRepo, never()).save(any());
        verify(userCardService, never()).consumeOnClassCheckin(anyLong(), anyLong(), anyString());
    }

    private static Schedule schedule(Long id) {
        Schedule schedule = new Schedule();
        schedule.setId(id);
        schedule.setName("JAZZ进阶");
        schedule.setTimeText("20:10-21:30");
        schedule.setTeacherName("老师");
        schedule.setRoom("A");
        schedule.setCampusId("1");
        return schedule;
    }
}
