package com.forget.academy.service;

import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.ClassArchiveRepo;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceFinalizeStudentTest {

    @Mock private CheckinService checkinService;
    @Mock private AppUserRepo appUserRepo;
    @Mock private ScheduleRepo scheduleRepo;
    @Mock private TeacherAttendanceRepo teacherAttendanceRepo;
    @Mock private EmployeeDutyRecordRepo employeeDutyRecordRepo;
    @Mock private ClassArchiveRepo classArchiveRepo;
    @Mock private TeacherService teacherService;

    private AttendanceService service;

    @BeforeEach
    void setUp() {
        service = new AttendanceService(
                appUserRepo,
                scheduleRepo,
                teacherAttendanceRepo,
                employeeDutyRecordRepo,
                classArchiveRepo,
                checkinService,
                teacherService
        );
    }

    @Test
    void finalizeStudentCheckin_canOverwriteMessage_evenIfManualCheckinReturnedImmutableMap() {
        when(checkinService.manualCheckin(eq(100L), eq(55L), eq("2026-09-08"), eq("管理员"), eq(CheckinService.SOURCE_CONFIRMED)))
                .thenReturn(Map.of("ok", true, "message", "签到成功", "record", Map.of()));

        Map<String, Object> result = assertDoesNotThrow(
                () -> service.finalizeStudentCheckin(100L, 55L, "2026-09-08", "管理员"));

        assertEquals("已确认到场", result.get("message"));
        verify(teacherService).syncArchiveCounts(55L, "2026-09-08");
    }

    @Test
    void finalizeStudentCheckin_worksWithMutableManualResult() {
        Map<String, Object> mutable = new LinkedHashMap<>();
        mutable.put("ok", true);
        mutable.put("message", "签到成功");
        when(checkinService.manualCheckin(anyLong(), anyLong(), anyString(), anyString(), any()))
                .thenReturn(mutable);

        Map<String, Object> result = service.finalizeStudentCheckin(1L, 2L, "2026-09-08", "前台");
        assertEquals("已确认到场", result.get("message"));
    }
}
