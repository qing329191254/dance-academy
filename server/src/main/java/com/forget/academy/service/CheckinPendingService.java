package com.forget.academy.service;

import com.forget.academy.common.AppRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.CheckinPending;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.service.CheckinSessionService.ValidatedScan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckinPendingService {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_REJECTED = "rejected";

    private final CheckinPendingRepo checkinPendingRepo;
    private final CheckinSessionService checkinSessionService;
    private final AttendanceService attendanceService;
    private final AppUserRepo appUserRepo;
    private final ScheduleRepo scheduleRepo;
    private final BookingRepo bookingRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final TeacherAttendanceRepo teacherAttendanceRepo;
    private final EmployeeDutyRecordRepo employeeDutyRecordRepo;

    @Transactional
    public Map<String, Object> submitScan(Long userId, String raw) {
        ValidatedScan validated = checkinSessionService.validateScanPayload(raw);
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        String role = normalizeRole(user.getRole());
        Schedule schedule = validated.schedule();
        String classDate = validated.classDate();
        validateRoleForSchedule(user, role, schedule, classDate);

        if (alreadyCheckedIn(userId, role, schedule.getId(), classDate)) {
            throw new BizException("本节课已签到，请勿重复扫描");
        }

        var existing = checkinPendingRepo.findByUserIdAndScheduleIdAndClassDate(userId, schedule.getId(), classDate);
        if (existing.isPresent()) {
            CheckinPending pending = existing.get();
            if (STATUS_PENDING.equals(pending.getStatus())) {
                return pendingResponse(pending, "已提交签到，请等待工作人员确认");
            }
            if (STATUS_CONFIRMED.equals(pending.getStatus())) {
                throw new BizException("本节课已签到，请勿重复扫描");
            }
        }

        CheckinPending pending = existing.orElseGet(CheckinPending::new);
        pending.setUserId(userId);
        pending.setRole(role);
        pending.setScheduleId(schedule.getId());
        pending.setClassDate(classDate);
        pending.setCampusId(resolveCampus(schedule));
        pending.setNickname(firstNonBlank(user.getNickname(), roleLabel(role)));
        pending.setClassName(schedule.getName());
        pending.setTimeText(schedule.getTimeText());
        pending.setTeacherName(schedule.getTeacherName());
        pending.setRoom(schedule.getRoom());
        pending.setStatus(STATUS_PENDING);
        pending.setCheckinSessionId(validated.session().getId());
        pending.setScannedAt(Instant.now());
        pending.setConfirmedAt(null);
        pending.setConfirmedByUserId(null);
        pending.setConfirmedByName(null);
        checkinPendingRepo.save(pending);
        return pendingResponse(pending, "已提交签到，请等待工作人员确认");
    }

    public PageResult<Map<String, Object>> listPending(String status, String classDate, Long scheduleId,
                                                       List<String> campusIds, String keyword, int page, int size) {
        String queryStatus = status == null || status.isBlank() ? STATUS_PENDING : status.trim();
        String queryDate = classDate == null ? "" : classDate.trim();
        String queryKeyword = keyword == null ? "" : keyword.trim();
        var pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 100));
        var result = checkinPendingRepo.search(queryStatus, queryDate, scheduleId, campusIds, queryKeyword, pageable);
        List<Map<String, Object>> list = new ArrayList<>();
        for (CheckinPending item : result.getContent()) {
            list.add(toMap(item));
        }
        return new PageResult<>(list, result.getTotalElements(), result.getNumber() + 1, result.getSize());
    }

    public List<Map<String, Object>> listForSession(Long scheduleId, String classDate, String status) {
        String queryStatus = status == null || status.isBlank() ? STATUS_PENDING : status.trim();
        return checkinPendingRepo.findByScheduleIdAndClassDateAndStatusOrderByScannedAtAsc(
                        scheduleId, normalizeDate(classDate), queryStatus)
                .stream().map(this::toMap).toList();
    }

    @Transactional
    public Map<String, Object> confirm(Long pendingId, Long operatorUserId, String operatorName) {
        CheckinPending pending = checkinPendingRepo.findById(pendingId)
                .orElseThrow(() -> new BizException("签到记录不存在"));
        if (!STATUS_PENDING.equals(pending.getStatus())) {
            throw new BizException("该记录已处理");
        }
        appUserRepo.findById(operatorUserId).ifPresent(operator -> {
            if (AppRoles.EMPLOYEE.equalsIgnoreCase(operator.getRole())
                    && operator.getCampusId() != null
                    && !operator.getCampusId().equals(pending.getCampusId())) {
                throw new BizException("无权确认其他校区的签到");
            }
        });
        if (alreadyCheckedIn(pending.getUserId(), pending.getRole(), pending.getScheduleId(), pending.getClassDate())) {
            pending.setStatus(STATUS_CONFIRMED);
            pending.setConfirmedAt(Instant.now());
            pending.setConfirmedByUserId(operatorUserId);
            pending.setConfirmedByName(operatorName);
            checkinPendingRepo.save(pending);
            return Map.of("ok", true, "message", "该用户已签到");
        }
        String operator = operatorName == null || operatorName.isBlank() ? "工作人员" : operatorName.trim();
        Map<String, Object> result = attendanceService.finalizeAfterConfirm(
                pending.getUserId(),
                pending.getRole(),
                pending.getScheduleId(),
                pending.getClassDate(),
                operator);
        pending.setStatus(STATUS_CONFIRMED);
        pending.setConfirmedAt(Instant.now());
        pending.setConfirmedByUserId(operatorUserId);
        pending.setConfirmedByName(operator);
        checkinPendingRepo.save(pending);
        result.put("pendingId", pending.getId());
        return result;
    }

    @Transactional
    public void reject(Long pendingId, Long operatorUserId, String operatorName) {
        CheckinPending pending = checkinPendingRepo.findById(pendingId)
                .orElseThrow(() -> new BizException("签到记录不存在"));
        if (!STATUS_PENDING.equals(pending.getStatus())) {
            throw new BizException("该记录已处理");
        }
        pending.setStatus(STATUS_REJECTED);
        pending.setConfirmedAt(Instant.now());
        pending.setConfirmedByUserId(operatorUserId);
        pending.setConfirmedByName(operatorName == null || operatorName.isBlank() ? "工作人员" : operatorName.trim());
        checkinPendingRepo.save(pending);
    }

    public boolean isPending(Long userId, Long scheduleId, String classDate) {
        return checkinPendingRepo.findByUserIdAndScheduleIdAndClassDate(userId, scheduleId, normalizeDate(classDate))
                .map(item -> STATUS_PENDING.equals(item.getStatus()))
                .orElse(false);
    }

    private boolean alreadyCheckedIn(Long userId, String role, Long scheduleId, String classDate) {
        String date = normalizeDate(classDate);
        return switch (role) {
            case AppRoles.TEACHER -> teacherAttendanceRepo.existsByUserIdAndScheduleIdAndClassDate(userId, scheduleId, date);
            case AppRoles.EMPLOYEE -> employeeDutyRecordRepo.existsByUserIdAndScheduleIdAndClassDate(userId, scheduleId, date);
            default -> practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(userId, String.valueOf(scheduleId), date);
        };
    }

    private void validateRoleForSchedule(AppUser user, String role, Schedule schedule, String classDate) {
        String date = normalizeDate(classDate);
        switch (role) {
            case AppRoles.TEACHER -> {
                if (user.getTeacherId() == null || !user.getTeacherId().equals(schedule.getTeacherId())) {
                    throw new BizException("这不是你的课程，无法签到");
                }
            }
            case AppRoles.EMPLOYEE -> {
                if (user.getCampusId() == null || user.getCampusId().isBlank()) {
                    throw new BizException("员工账号未绑定校区");
                }
                if (!user.getCampusId().equals(resolveCampus(schedule))) {
                    throw new BizException("该课程不属于你的值班校区");
                }
            }
            default -> bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                    user.getId(), schedule.getId(), date, "待上课")
                    .orElseThrow(() -> new BizException("你未预约本节课，无法签到"));
        }
    }

    private Map<String, Object> pendingResponse(CheckinPending pending, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ok", true);
        map.put("pending", true);
        map.put("message", message);
        map.put("pendingId", pending.getId());
        map.put("status", pending.getStatus());
        return map;
    }

    private Map<String, Object> toMap(CheckinPending pending) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pending.getId());
        map.put("userId", pending.getUserId());
        map.put("role", pending.getRole());
        map.put("roleLabel", roleLabel(pending.getRole()));
        map.put("nickname", pending.getNickname());
        map.put("scheduleId", pending.getScheduleId());
        map.put("classDate", pending.getClassDate());
        map.put("className", pending.getClassName());
        map.put("timeText", pending.getTimeText());
        map.put("teacherName", pending.getTeacherName());
        map.put("room", pending.getRoom());
        map.put("campusId", pending.getCampusId());
        map.put("status", pending.getStatus());
        map.put("scannedAt", pending.getScannedAt() == null ? null : pending.getScannedAt().toEpochMilli());
        map.put("confirmedAt", pending.getConfirmedAt() == null ? null : pending.getConfirmedAt().toEpochMilli());
        map.put("confirmedByName", pending.getConfirmedByName());
        return map;
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return AppRoles.STUDENT;
        }
        return role.trim().toLowerCase();
    }

    private static String normalizeDate(String classDate) {
        return classDate == null || classDate.isBlank()
                ? java.time.LocalDate.now().toString()
                : classDate.trim();
    }

    private static String resolveCampus(Schedule schedule) {
        if (schedule.getCampusId() == null || schedule.getCampusId().isBlank()) {
            return CampusIds.DEFAULT;
        }
        return schedule.getCampusId();
    }

    private static String roleLabel(String role) {
        return switch (normalizeRole(role)) {
            case AppRoles.TEACHER -> "教师";
            case AppRoles.EMPLOYEE -> "员工";
            default -> "学员";
        };
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
