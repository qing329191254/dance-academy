package com.forget.academy.service;

import com.forget.academy.common.CheckinTypes;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.CheckinPending;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckinPendingConfirmSuccessTest {

    @Mock private CheckinPendingRepo checkinPendingRepo;
    @Mock private CheckinSessionService checkinSessionService;
    @Mock private AttendanceService attendanceService;
    @Mock private AppUserRepo appUserRepo;
    @Mock private ScheduleRepo scheduleRepo;
    @Mock private BookingRepo bookingRepo;
    @Mock private PracticeRecordRepo practiceRecordRepo;
    @Mock private TeacherAttendanceRepo teacherAttendanceRepo;
    @Mock private EmployeeDutyRecordRepo employeeDutyRecordRepo;
    @Mock private AdminAccessService adminAccessService;

    private CheckinPendingService service;

    @BeforeEach
    void setUp() {
        service = new CheckinPendingService(
                checkinPendingRepo,
                checkinSessionService,
                attendanceService,
                appUserRepo,
                scheduleRepo,
                bookingRepo,
                practiceRecordRepo,
                teacherAttendanceRepo,
                employeeDutyRecordRepo,
                adminAccessService
        );
    }

    /** 回归：曾用 Map.of 再 put pendingId → UnsupportedOperationException → 服务器异常 */
    @Test
    void confirmByAdmin_firstTimeStudent_copiesImmutableAttendanceResultBeforePut() {
        CheckinPending pending = pending(11L, "1");
        when(checkinPendingRepo.findById(11L)).thenReturn(Optional.of(pending));
        when(practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(anyLong(), anyString(), anyString()))
                .thenReturn(false);
        when(bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                eq(100L), eq(55L), eq("2026-09-08"), eq("待上课")))
                .thenReturn(Optional.of(new Booking()));
        when(attendanceService.finalizeAfterConfirm(
                eq(100L), eq(CheckinTypes.CLASS), eq(55L), eq("2026-09-08"), eq("超级管理员")))
                .thenReturn(Map.of("ok", true, "message", "已确认到场"));

        Map<String, Object> result = assertDoesNotThrow(
                () -> service.confirmByAdmin(11L, 7L, "超级管理员"));

        assertTrue((Boolean) result.get("ok"));
        assertEquals(11L, result.get("pendingId"));
        assertEquals("已确认到场", result.get("message"));
        assertEquals(CheckinPendingService.STATUS_CONFIRMED, pending.getStatus());
        verify(adminAccessService).assertCanAccessCampus("1");
        verify(checkinPendingRepo).save(pending);
    }

    @Test
    void confirmByAdmin_firstTimeStudent_alsoWorksWithMutableAttendanceResult() {
        CheckinPending pending = pending(12L, "4");
        when(checkinPendingRepo.findById(12L)).thenReturn(Optional.of(pending));
        when(practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(anyLong(), anyString(), anyString()))
                .thenReturn(false);
        when(bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                eq(100L), eq(55L), eq("2026-09-08"), eq("待上课")))
                .thenReturn(Optional.of(new Booking()));
        Map<String, Object> mutable = new LinkedHashMap<>();
        mutable.put("ok", true);
        mutable.put("message", "已确认到场");
        when(attendanceService.finalizeAfterConfirm(
                eq(100L), eq(CheckinTypes.CLASS), eq(55L), eq("2026-09-08"), eq("超管")))
                .thenReturn(mutable);

        Map<String, Object> result = service.confirmByAdmin(12L, 7L, "超管");

        assertEquals(12L, result.get("pendingId"));
        assertEquals(CheckinPendingService.STATUS_CONFIRMED, pending.getStatus());
    }

    @Test
    void confirmByAdmin_rejectsWhenBookingAlreadyCancelled() {
        CheckinPending pending = pending(13L, "1");
        when(checkinPendingRepo.findById(13L)).thenReturn(Optional.of(pending));
        when(practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(anyLong(), anyString(), anyString()))
                .thenReturn(false);
        when(bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                eq(100L), eq(55L), eq("2026-09-08"), eq("待上课")))
                .thenReturn(Optional.empty());

        var ex = assertThrows(com.forget.academy.common.BizException.class,
                () -> service.confirmByAdmin(13L, 7L, "管理员"));
        assertEquals("预约已取消，无法确认签到", ex.getMessage());
        verify(attendanceService, never()).finalizeAfterConfirm(anyLong(), anyString(), anyLong(), anyString(), anyString());
        assertEquals(CheckinPendingService.STATUS_PENDING, pending.getStatus());
    }

    private static CheckinPending pending(Long id, String campusId) {
        CheckinPending pending = new CheckinPending();
        pending.setId(id);
        pending.setUserId(100L);
        pending.setCampusId(campusId);
        pending.setStatus(CheckinPendingService.STATUS_PENDING);
        pending.setCheckinType(CheckinTypes.CLASS);
        pending.setScheduleId(55L);
        pending.setClassDate("2026-09-08");
        return pending;
    }
}
