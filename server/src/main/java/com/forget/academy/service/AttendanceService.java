package com.forget.academy.service;

import com.forget.academy.common.AppRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.common.ClassScheduleTimeUtil;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.ClassArchive;
import com.forget.academy.entity.EmployeeDutyRecord;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.TeacherAttendance;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.ClassArchiveRepo;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final String STATUS_ON_TIME = "on_time";
    private static final String STATUS_LATE = "late";
    private static final int EMPLOYEE_EARLY_MINUTES = 5;

    private final AppUserRepo appUserRepo;
    private final ScheduleRepo scheduleRepo;
    private final TeacherAttendanceRepo teacherAttendanceRepo;
    private final EmployeeDutyRecordRepo employeeDutyRecordRepo;
    private final ClassArchiveRepo classArchiveRepo;
    private final CheckinService checkinService;
    private final TeacherService teacherService;

    @Transactional
    public Map<String, Object> checkinByRole(Long userId, String raw) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        String role = user.getRole() == null ? AppRoles.STUDENT : user.getRole().trim().toLowerCase();
        return switch (role) {
            case AppRoles.TEACHER -> teacherAttendanceCheckin(userId, raw);
            case AppRoles.EMPLOYEE -> employeeDutyCheckin(userId, raw);
            default -> checkinService.checkin(userId, raw);
        };
    }

    @Transactional
    public Map<String, Object> teacherAttendanceCheckin(Long userId, String raw) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        if (!AppRoles.TEACHER.equalsIgnoreCase(user.getRole())) {
            throw new BizException("当前账号不是老师");
        }
        if (user.getTeacherId() == null) {
            throw new BizException("老师账号未绑定课表档案");
        }
        Map<String, String> session = checkinService.resolveSession(raw);
        Long scheduleId = Long.parseLong(session.get("id"));
        String classDate = session.get("date");
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        if (!user.getTeacherId().equals(schedule.getTeacherId())) {
            throw new BizException("这不是你的课程，无法考勤签到");
        }
        if (teacherAttendanceRepo.existsByUserIdAndScheduleIdAndClassDate(userId, scheduleId, classDate)) {
            throw new BizException("本节课已考勤签到，请勿重复扫描");
        }

        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime classStart = ClassScheduleTimeUtil.classStartAt(classDate, schedule.getTimeText());
        int lateMinutes = ClassScheduleTimeUtil.minutesLate(classStart, now);
        String status = lateMinutes > 0 ? STATUS_LATE : STATUS_ON_TIME;

        TeacherAttendance record = new TeacherAttendance();
        record.setUserId(userId);
        record.setTeacherId(user.getTeacherId());
        record.setScheduleId(scheduleId);
        record.setClassDate(classDate);
        record.setClassName(schedule.getName());
        record.setTimeText(schedule.getTimeText());
        record.setCampusId(resolveCampus(schedule));
        record.setStatus(status);
        record.setLateMinutes(lateMinutes);
        record.setCheckedAt(Instant.now());
        try {
            teacherAttendanceRepo.save(record);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("本节课已考勤签到，请勿重复扫描");
        }

        touchClassArchive(user.getTeacherId(), schedule, classDate, session.get("duration"));
        teacherService.syncArchiveCounts(scheduleId, classDate);

        String message = STATUS_LATE.equals(status)
                ? schedule.getName() + " 考勤成功（迟到 " + lateMinutes + " 分钟）"
                : schedule.getName() + " 考勤签到成功";
        return Map.of("ok", true, "message", message, "record", toTeacherMap(record));
    }

    @Transactional
    public Map<String, Object> employeeDutyCheckin(Long userId, String raw) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        if (!AppRoles.EMPLOYEE.equalsIgnoreCase(user.getRole())) {
            throw new BizException("当前账号不是员工");
        }
        if (user.getCampusId() == null || user.getCampusId().isBlank()) {
            throw new BizException("员工账号未绑定校区，请联系管理员");
        }
        Map<String, String> session = checkinService.resolveSession(raw);
        Long scheduleId = Long.parseLong(session.get("id"));
        String classDate = session.get("date");
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        String campusId = resolveCampus(schedule);
        if (!user.getCampusId().equals(campusId)) {
            throw new BizException("该课程不属于你的值班校区");
        }
        if (employeeDutyRecordRepo.existsByUserIdAndScheduleIdAndClassDate(userId, scheduleId, classDate)) {
            throw new BizException("本节课已值班签到，请勿重复扫描");
        }

        LocalDateTime now = LocalDateTime.now(ZONE);
        LocalDateTime classStart = ClassScheduleTimeUtil.classStartAt(classDate, schedule.getTimeText());
        LocalDateTime deadline = classStart.minusMinutes(EMPLOYEE_EARLY_MINUTES);
        int lateMinutes = ClassScheduleTimeUtil.minutesLate(deadline, now);
        String status = lateMinutes > 0 ? STATUS_LATE : STATUS_ON_TIME;

        EmployeeDutyRecord record = new EmployeeDutyRecord();
        record.setUserId(userId);
        record.setScheduleId(scheduleId);
        record.setClassDate(classDate);
        record.setClassName(schedule.getName());
        record.setTimeText(schedule.getTimeText());
        record.setCampusId(campusId);
        record.setStatus(status);
        record.setLateMinutes(lateMinutes);
        record.setCheckedAt(Instant.now());
        try {
            employeeDutyRecordRepo.save(record);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("本节课已值班签到，请勿重复扫描");
        }

        String message = STATUS_LATE.equals(status)
                ? "值班签到成功（迟到 " + lateMinutes + " 分钟）"
                : "值班签到成功";
        return Map.of("ok", true, "message", message, "record", toEmployeeMap(record));
    }

    public List<Map<String, Object>> myTeacherAttendance(Long userId) {
        return teacherAttendanceRepo.findByUserIdOrderByCheckedAtDesc(userId).stream().map(this::toTeacherMap).toList();
    }

    public List<Map<String, Object>> myEmployeeDuty(Long userId) {
        return employeeDutyRecordRepo.findByUserIdOrderByCheckedAtDesc(userId).stream().map(this::toEmployeeMap).toList();
    }

    private void touchClassArchive(Long teacherId, Schedule schedule, String classDate, String duration) {
        ClassArchive archive = classArchiveRepo
                .findByTeacherIdAndScheduleIdAndClassDate(teacherId, schedule.getId(), classDate)
                .orElseGet(ClassArchive::new);
        archive.setTeacherId(teacherId);
        archive.setScheduleId(schedule.getId());
        archive.setClassDate(classDate);
        archive.setName(schedule.getName());
        archive.setTimeText(schedule.getTimeText());
        archive.setRoom(schedule.getRoom());
        archive.setCampusId(resolveCampus(schedule));
        archive.setDuration(duration == null || duration.isBlank() ? "75分钟" : duration);
        archive.setTeacherCheckedAt(Instant.now());
        classArchiveRepo.save(archive);
    }

    private String resolveCampus(Schedule schedule) {
        if (schedule.getCampusId() == null || schedule.getCampusId().isBlank()) {
            return CampusIds.DEFAULT;
        }
        return schedule.getCampusId();
    }

    private Map<String, Object> toTeacherMap(TeacherAttendance record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("scheduleId", record.getScheduleId());
        map.put("className", record.getClassName());
        map.put("date", record.getClassDate());
        map.put("time", record.getTimeText());
        map.put("campusId", record.getCampusId());
        map.put("status", record.getStatus());
        map.put("lateMinutes", record.getLateMinutes());
        map.put("checkedAt", record.getCheckedAt() == null ? null : record.getCheckedAt().toEpochMilli());
        return map;
    }

    private Map<String, Object> toEmployeeMap(EmployeeDutyRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("scheduleId", record.getScheduleId());
        map.put("className", record.getClassName());
        map.put("date", record.getClassDate());
        map.put("time", record.getTimeText());
        map.put("campusId", record.getCampusId());
        map.put("status", record.getStatus());
        map.put("lateMinutes", record.getLateMinutes());
        map.put("checkedAt", record.getCheckedAt() == null ? null : record.getCheckedAt().toEpochMilli());
        return map;
    }
}
