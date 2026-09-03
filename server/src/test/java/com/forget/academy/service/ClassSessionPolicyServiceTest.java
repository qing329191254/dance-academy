package com.forget.academy.service;

import com.forget.academy.entity.Booking;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.ClassSessionCancelRepo;
import com.forget.academy.repo.ScheduleRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassSessionPolicyServiceTest {

    @Mock
    private ScheduleRepo scheduleRepo;
    @Mock
    private BookingRepo bookingRepo;
    @Mock
    private ClassSessionCancelRepo classSessionCancelRepo;
    @Mock
    private BookingService bookingService;

    private ClassSessionPolicyService service;

    @BeforeEach
    void setUp() {
        service = new ClassSessionPolicyService(scheduleRepo, bookingRepo, classSessionCancelRepo, bookingService);
    }

    @Test
    void lowEnrollment_cancelsWhenBelowMinInsideTwoHourWindow() {
        Schedule schedule = groupSchedule(1L, weekdayOf(LocalDate.of(2026, 9, 9)), "18:10-19:30", 4);
        when(scheduleRepo.findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc("group"))
                .thenReturn(List.of(schedule));
        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(1L, "2026-09-09")).thenReturn(false);
        when(bookingRepo.countByScheduleIdAndClassDateAndStatus(1L, "2026-09-09", "待上课")).thenReturn(2L);

        // 开课前约 1.5 小时：已过取消节点，未到开课
        LocalDateTime now = LocalDateTime.of(2026, 9, 9, 16, 40);
        service.checkLowEnrollment(now);

        verify(bookingService).cancelSessionForLowEnrollment(schedule, "2026-09-09", 2, 4);
    }

    @Test
    void lowEnrollment_skipsWhenEnoughPeople() {
        Schedule schedule = groupSchedule(2L, weekdayOf(LocalDate.of(2026, 9, 9)), "18:10-19:30", 4);
        when(scheduleRepo.findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc("group"))
                .thenReturn(List.of(schedule));
        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(2L, "2026-09-09")).thenReturn(false);
        when(bookingRepo.countByScheduleIdAndClassDateAndStatus(2L, "2026-09-09", "待上课")).thenReturn(4L);

        service.checkLowEnrollment(LocalDateTime.of(2026, 9, 9, 16, 40));

        verify(bookingService, never()).cancelSessionForLowEnrollment(any(), anyString(), anyInt(), anyInt());
    }

    @Test
    void lowEnrollment_skipsWhenMinDisabled() {
        Schedule schedule = groupSchedule(3L, weekdayOf(LocalDate.of(2026, 9, 9)), "18:10-19:30", 0);
        when(scheduleRepo.findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc("group"))
                .thenReturn(List.of(schedule));

        service.checkLowEnrollment(LocalDateTime.of(2026, 9, 9, 16, 40));

        verify(bookingService, never()).cancelSessionForLowEnrollment(any(), anyString(), anyInt(), anyInt());
        verify(bookingRepo, never()).countByScheduleIdAndClassDateAndStatus(anyLong(), anyString(), anyString());
    }

    @Test
    void lowEnrollment_skipsBeforeTwoHourMark() {
        Schedule schedule = groupSchedule(4L, weekdayOf(LocalDate.of(2026, 9, 9)), "18:10-19:30", 4);
        when(scheduleRepo.findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc("group"))
                .thenReturn(List.of(schedule));

        // 开课前 3 小时，尚未到取消窗口
        service.checkLowEnrollment(LocalDateTime.of(2026, 9, 9, 15, 10));

        verify(bookingService, never()).cancelSessionForLowEnrollment(any(), anyString(), anyInt(), anyInt());
    }

    @Test
    void noShow_settlesAfterGrace() {
        Booking booking = new Booking();
        booking.setId(99L);
        booking.setScheduleId(1L);
        booking.setClassDate("2026-09-09");
        booking.setTimeText("18:10-19:30");
        booking.setCardConsumed(false);
        when(bookingRepo.findGroupPendingForNoShowSettle()).thenReturn(List.of(booking));
        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(1L, "2026-09-09")).thenReturn(false);

        // 开课后 61 分钟
        service.settleNoShows(LocalDateTime.of(2026, 9, 9, 19, 11));

        verify(bookingService).settleNoShow(99L);
    }

    @Test
    void noShow_skipsCancelledSession() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setScheduleId(1L);
        booking.setClassDate("2026-09-09");
        booking.setTimeText("18:10-19:30");
        booking.setCardConsumed(false);
        when(bookingRepo.findGroupPendingForNoShowSettle()).thenReturn(List.of(booking));
        when(classSessionCancelRepo.existsByScheduleIdAndClassDate(1L, "2026-09-09")).thenReturn(true);

        service.settleNoShows(LocalDateTime.of(2026, 9, 9, 19, 11));

        verify(bookingService, never()).settleNoShow(anyLong());
    }

    @Test
    void studentCancelLock_usesTwoHours() {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(2026, 9, 9), LocalTime.of(18, 10));
        LocalDateTime lockAt = start.minusHours(2);
        assertEquals(LocalDateTime.of(2026, 9, 9, 16, 10), lockAt);
    }

    private static Schedule groupSchedule(Long id, int weekday, String time, int min) {
        Schedule s = new Schedule();
        s.setId(id);
        s.setType("group");
        s.setEnabled(true);
        s.setWeekday(weekday);
        s.setTimeText(time);
        s.setMinEnrollment(min);
        return s;
    }

    private static int weekdayOf(LocalDate date) {
        var day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SUNDAY ? 0 : day.getValue();
    }
}
