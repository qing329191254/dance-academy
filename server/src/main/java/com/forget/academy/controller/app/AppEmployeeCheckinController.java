package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.entity.AppUser;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.CheckinPendingService;
import com.forget.academy.service.CheckinSessionService;
import com.forget.academy.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app/employee/checkin")
@RequiredArgsConstructor
public class AppEmployeeCheckinController {
    private final EmployeeService employeeService;
    private final CheckinSessionService checkinSessionService;
    private final CheckinPendingService checkinPendingService;

    @GetMapping("/schedules")
    public ApiResponse<List<Map<String, Object>>> schedules(@RequestParam(required = false) String date) {
        AppUser user = employeeService.requireEmployee(AuthContext.requireApp().id());
        return ApiResponse.ok(checkinSessionService.listSchedulesForCampus(user.getCampusId(), date));
    }

    @PostMapping("/sessions")
    public ApiResponse<?> openSession(@RequestBody Map<String, Object> body) {
        AppUser user = employeeService.requireEmployee(AuthContext.requireApp().id());
        Long scheduleId = parseLong(body.get("scheduleId"));
        String classDate = body.get("classDate") == null ? "" : String.valueOf(body.get("classDate")).trim();
        if (scheduleId == null) {
            throw new com.forget.academy.common.BizException("请选择课程");
        }
        var session = checkinSessionService.openSession(user.getId(), scheduleId, classDate);
        return ApiResponse.ok(checkinSessionService.toSessionMapPublic(session));
    }

    @PostMapping("/sessions/{id}/close")
    public ApiResponse<Void> closeSession(@PathVariable Long id) {
        employeeService.requireEmployee(AuthContext.requireApp().id());
        checkinSessionService.closeSession(id);
        return ApiResponse.ok();
    }

    @GetMapping("/sessions/{id}/payload")
    public ApiResponse<?> sessionPayload(@PathVariable Long id) {
        employeeService.requireEmployee(AuthContext.requireApp().id());
        return ApiResponse.ok(checkinSessionService.buildQrPayload(id));
    }

    @GetMapping("/sessions/active")
    public ApiResponse<?> activeSession(@RequestParam Long scheduleId,
                                        @RequestParam(required = false) String date) {
        employeeService.requireEmployee(AuthContext.requireApp().id());
        return ApiResponse.ok(checkinSessionService.getActiveSession(scheduleId, date));
    }

    @GetMapping("/pending")
    public ApiResponse<?> pendingList(@RequestParam Long scheduleId,
                                      @RequestParam(required = false) String date,
                                      @RequestParam(defaultValue = "pending") String status) {
        AppUser user = employeeService.requireEmployee(AuthContext.requireApp().id());
        return ApiResponse.ok(checkinPendingService.listForSession(scheduleId, date, status));
    }

    @PostMapping("/pending/{id}/confirm")
    public ApiResponse<?> confirm(@PathVariable Long id) {
        AppUser user = employeeService.requireEmployee(AuthContext.requireApp().id());
        String name = user.getNickname() == null || user.getNickname().isBlank() ? "员工" : user.getNickname();
        return ApiResponse.ok(checkinPendingService.confirm(id, user.getId(), name));
    }

    @PostMapping("/pending/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        AppUser user = employeeService.requireEmployee(AuthContext.requireApp().id());
        String name = user.getNickname() == null || user.getNickname().isBlank() ? "员工" : user.getNickname();
        checkinPendingService.reject(id, user.getId(), name);
        return ApiResponse.ok();
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
