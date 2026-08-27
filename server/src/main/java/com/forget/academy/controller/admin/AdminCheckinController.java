package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.service.AdminAccessService;
import com.forget.academy.service.CheckinPendingService;
import com.forget.academy.service.CheckinSessionService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCheckinController {
    private final CheckinSessionService checkinSessionService;
    private final CheckinPendingService checkinPendingService;
    private final AdminAccessService adminAccessService;
    private final ScheduleRepo scheduleRepo;

    @PostMapping("/checkin-sessions")
    public ApiResponse<?> openSession(@RequestBody Map<String, Object> body) {
        Long scheduleId = parseLong(body.get("scheduleId"));
        String classDate = body.get("classDate") == null ? "" : String.valueOf(body.get("classDate")).trim();
        if (scheduleId == null) {
            throw new com.forget.academy.common.BizException("请选择课程");
        }
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(
                () -> new com.forget.academy.common.BizException("课表不存在"));
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        var admin = adminAccessService.currentAdmin();
        var session = checkinSessionService.openSession(admin.getId(), scheduleId, classDate);
        Map<String, Object> map = checkinSessionService.toSessionMapPublic(session);
        map.put("qr", checkinSessionService.buildQrPayload(session.getId()));
        return ApiResponse.ok(map);
    }

    @GetMapping("/checkin-sessions/{id}/payload")
    public ApiResponse<?> sessionPayload(@PathVariable Long id) {
        return ApiResponse.ok(checkinSessionService.buildQrPayload(id));
    }

    @PostMapping("/checkin-sessions/{id}/close")
    public ApiResponse<Void> closeSession(@PathVariable Long id) {
        checkinSessionService.closeSession(id);
        return ApiResponse.ok();
    }

    @GetMapping("/checkin-pending")
    public ApiResponse<PageResult<Map<String, Object>>> pendingList(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(required = false) String classDate,
            @RequestParam(required = false) Long scheduleId,
            @RequestParam(required = false) String campusId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var campuses = adminAccessService.resolveCampusScope(campusId);
        return ApiResponse.ok(checkinPendingService.listPending(
                status, classDate, scheduleId, campuses, keyword, page, size));
    }

    @PostMapping("/checkin-pending/{id}/confirm")
    public ApiResponse<?> confirm(@PathVariable Long id) {
        var admin = adminAccessService.currentAdmin();
        String name = admin.getName() == null || admin.getName().isBlank() ? admin.getUsername() : admin.getName();
        return ApiResponse.ok(checkinPendingService.confirm(id, admin.getId(), name));
    }

    @PostMapping("/checkin-pending/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        var admin = adminAccessService.currentAdmin();
        String name = admin.getName() == null || admin.getName().isBlank() ? admin.getUsername() : admin.getName();
        checkinPendingService.reject(id, admin.getId(), name);
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
