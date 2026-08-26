package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.AttendanceService;
import com.forget.academy.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/app/employee")
@RequiredArgsConstructor
public class AppEmployeeController {
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    @GetMapping("/profile")
    public ApiResponse<?> profile() {
        return ApiResponse.ok(employeeService.profile(AuthContext.requireApp().id()));
    }

    @GetMapping("/duty-records")
    public ApiResponse<?> dutyRecords() {
        return ApiResponse.ok(attendanceService.myEmployeeDuty(AuthContext.requireApp().id()));
    }

    @GetMapping("/weekly-reports")
    public ApiResponse<?> weeklyReports() {
        return ApiResponse.ok(employeeService.myReports(AuthContext.requireApp().id()));
    }

    @PostMapping("/weekly-reports")
    public ApiResponse<?> submitWeeklyReport(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(employeeService.submitReport(
                AuthContext.requireApp().id(),
                body.get("weekLabel"),
                body.get("content")));
    }

    @GetMapping("/performance")
    public ApiResponse<?> performance() {
        return ApiResponse.ok(employeeService.myPerformance(AuthContext.requireApp().id()));
    }
}
