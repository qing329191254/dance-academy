package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.AttendanceService;
import com.forget.academy.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/app/teacher")
@RequiredArgsConstructor
public class AppTeacherController {
    private final TeacherService teacherService;
    private final AttendanceService attendanceService;

    @GetMapping("/schedules")
    public ApiResponse<?> schedules(@RequestParam(required = false) String date) {
        return ApiResponse.ok(teacherService.listSchedules(AuthContext.requireApp().id(), date));
    }

    @GetMapping("/stats")
    public ApiResponse<?> stats() {
        return ApiResponse.ok(teacherService.stats(AuthContext.requireApp().id()));
    }

    @GetMapping("/archives")
    public ApiResponse<?> archives(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(teacherService.listArchives(AuthContext.requireApp().id(), page, size));
    }

    @GetMapping("/archives/{id}")
    public ApiResponse<?> archiveDetail(@PathVariable Long id) {
        return ApiResponse.ok(teacherService.archiveDetail(AuthContext.requireApp().id(), id));
    }

    @GetMapping("/attendance")
    public ApiResponse<?> attendance() {
        return ApiResponse.ok(attendanceService.myTeacherAttendance(AuthContext.requireApp().id()));
    }

    @GetMapping("/roster")
    public ApiResponse<?> roster(@RequestParam Long scheduleId, @RequestParam String date) {
        return ApiResponse.ok(teacherService.classRoster(AuthContext.requireApp().id(), scheduleId, date));
    }

    @PostMapping("/roster/checkin")
    public ApiResponse<?> manualCheckin(@RequestBody Map<String, Object> body) {
        Long userId = parseLong(body.get("userId"));
        Long scheduleId = parseLong(body.get("scheduleId"));
        String classDate = body.get("classDate") == null ? "" : String.valueOf(body.get("classDate")).trim();
        if (userId == null || scheduleId == null) {
            throw new com.forget.academy.common.BizException("参数不完整");
        }
        return ApiResponse.ok(teacherService.manualStudentCheckin(
                AuthContext.requireApp().id(), userId, scheduleId, classDate));
    }

    @PostMapping("/checkin")
    public ApiResponse<?> checkin(@RequestBody Map<String, String> body) {
        String payload = body == null ? null : body.get("payload");
        return ApiResponse.ok(teacherService.teacherCheckin(AuthContext.requireApp().id(), payload));
    }

    private static Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
