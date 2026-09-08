package com.forget.academy.service;

import com.forget.academy.common.AppRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CheckinTypes;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.CheckinPending;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import com.forget.academy.repo.TeacherRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckinPendingConfirmAccessTest {

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

    @Test
    void confirmByAdmin_usesAdminCampusScope_andDoesNotLookupAppUser() {
        CheckinPending pending = pending(1L, "wanda");
        when(checkinPendingRepo.findById(1L)).thenReturn(Optional.of(pending));
        when(practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(anyLong(), anyString(), anyString()))
                .thenReturn(true);

        Map<String, Object> result = service.confirmByAdmin(1L, 7L, "超级管理员");

        verify(adminAccessService).assertCanAccessCampus("wanda");
        verify(appUserRepo, never()).findById(any());
        assertTrue((Boolean) result.get("ok"));
        assertEquals(CheckinPendingService.STATUS_CONFIRMED, pending.getStatus());
        assertEquals(7L, pending.getConfirmedByUserId());
    }

    @Test
    void confirmByAdmin_forwardsCampusDenial() {
        CheckinPending pending = pending(1L, "other-campus");
        when(checkinPendingRepo.findById(1L)).thenReturn(Optional.of(pending));
        doThrow(new BizException(403, "无权访问该校区"))
                .when(adminAccessService).assertCanAccessCampus("other-campus");

        BizException ex = assertThrows(BizException.class,
                () -> service.confirmByAdmin(1L, 7L, "校区管理员"));
        assertEquals(403, ex.getCode());
        verify(appUserRepo, never()).findById(any());
        verify(practiceRecordRepo, never()).existsByUserIdAndSessionIdAndClassDate(anyLong(), anyString(), anyString());
    }

    @Test
    void confirmByEmployee_rejectsOtherCampus() {
        CheckinPending pending = pending(2L, "wanda");
        when(checkinPendingRepo.findById(2L)).thenReturn(Optional.of(pending));

        AppUser employee = new AppUser();
        employee.setId(88L);
        employee.setRole(AppRoles.EMPLOYEE);
        employee.setCampusId("other");
        when(appUserRepo.findById(88L)).thenReturn(Optional.of(employee));

        BizException ex = assertThrows(BizException.class,
                () -> service.confirmByEmployee(2L, 88L, "前台"));
        assertEquals("无权确认其他校区的签到", ex.getMessage());
    }

    @Test
    void rejectByEmployee_alsoChecksCampus() {
        CheckinPending pending = pending(3L, "wanda");
        when(checkinPendingRepo.findById(3L)).thenReturn(Optional.of(pending));

        AppUser employee = new AppUser();
        employee.setId(88L);
        employee.setRole(AppRoles.EMPLOYEE);
        employee.setCampusId("other");
        when(appUserRepo.findById(88L)).thenReturn(Optional.of(employee));

        BizException ex = assertThrows(BizException.class,
                () -> service.rejectByEmployee(3L, 88L, "前台"));
        assertEquals("无权确认其他校区的签到", ex.getMessage());
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
