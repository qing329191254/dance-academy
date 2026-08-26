package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.ClassArchive;
import com.forget.academy.entity.PracticeRecord;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.ClassArchiveRepo;
import com.forget.academy.repo.EmployeePerformanceRepo;
import com.forget.academy.repo.EmployeeProfileRepo;
import com.forget.academy.repo.EmployeeWeeklyReportRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import com.forget.academy.repo.TeacherRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private static final String STATUS_PENDING = "待上课";

    private final AppUserRepo appUserRepo;
    private final TeacherRepo teacherRepo;
    private final ScheduleRepo scheduleRepo;
    private final BookingRepo bookingRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final ClassArchiveRepo classArchiveRepo;
    private final TeacherAttendanceRepo teacherAttendanceRepo;
    private final CheckinService checkinService;
    private final CheckinPendingRepo checkinPendingRepo;

    public AppUser requireTeacherUser(Long userId) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        if (!"teacher".equalsIgnoreCase(user.getRole())) {
            throw new BizException("当前账号不是老师");
        }
        if (user.getTeacherId() == null) {
            throw new BizException("老师账号未绑定课表档案");
        }
        if (!teacherRepo.existsById(user.getTeacherId())) {
            throw new BizException("绑定的老师档案不存在");
        }
        return user;
    }

    public List<Map<String, Object>> listSchedules(Long userId, String date) {
        AppUser user = requireTeacherUser(userId);
        String classDate = date == null || date.isBlank() ? LocalDate.now().toString() : date.trim();
        int weekday = toWeekday(LocalDate.parse(classDate));
        List<Schedule> schedules = scheduleRepo.findByTeacherIdAndEnabledTrueOrderBySortOrderAscIdAsc(user.getTeacherId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Schedule schedule : schedules) {
            if ("group".equals(schedule.getType()) && schedule.getWeekday() != null && !schedule.getWeekday().equals(weekday)) {
                continue;
            }
            result.add(toScheduleRow(schedule, classDate));
        }
        return result;
    }

    public Map<String, Object> stats(Long userId) {
        AppUser user = requireTeacherUser(userId);
        Long teacherId = user.getTeacherId();
        long totalSessions = teacherAttendanceRepo.countByTeacherId(teacherId);
        String monthPrefix = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        long monthSessions = teacherAttendanceRepo.countByTeacherIdAndClassDateStartingWith(teacherId, monthPrefix);
        List<ClassArchive> archives = classArchiveRepo.findByTeacherIdOrderByClassDateDescIdDesc(teacherId);
        int totalMinutes = teacherAttendanceRepo.findByTeacherId(teacherId).stream()
                .mapToInt(item -> parseMinutes(item.getTimeText()))
                .sum();
        int monthMinutes = teacherAttendanceRepo.findByTeacherIdAndClassDateStartingWith(teacherId, monthPrefix).stream()
                .mapToInt(item -> parseMinutes(item.getTimeText()))
                .sum();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalSessions", totalSessions);
        map.put("monthSessions", monthSessions);
        map.put("totalHours", formatHours(totalMinutes));
        map.put("monthHours", formatHours(monthMinutes));
        map.put("totalStudents", archives.stream().mapToInt(item -> safeInt(item.getCheckedInCount())).sum());
        map.put("teacherName", teacherRepo.findById(teacherId).map(com.forget.academy.entity.Teacher::getName).orElse(""));
        return map;
    }

    public Map<String, Object> listArchives(Long userId, int page, int size) {
        AppUser user = requireTeacherUser(userId);
        var pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 50));
        var result = classArchiveRepo.findByTeacherIdOrderByClassDateDescIdDesc(user.getTeacherId(), pageable);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", result.getContent().stream().map(this::toArchiveMap).toList());
        data.put("total", result.getTotalElements());
        return data;
    }

    public Map<String, Object> archiveDetail(Long userId, Long archiveId) {
        AppUser user = requireTeacherUser(userId);
        ClassArchive archive = classArchiveRepo.findById(archiveId)
                .orElseThrow(() -> new BizException("课堂档案不存在"));
        if (!user.getTeacherId().equals(archive.getTeacherId())) {
            throw new BizException("无权查看该课堂档案");
        }
        return buildRosterDetail(archive.getScheduleId(), archive.getClassDate(), toArchiveMap(archive));
    }

    public Map<String, Object> classRoster(Long userId, Long scheduleId, String classDate) {
        AppUser user = requireTeacherUser(userId);
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        if (!user.getTeacherId().equals(schedule.getTeacherId())) {
            throw new BizException("这不是你的课程");
        }
        String date = normalizeDate(classDate);
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("scheduleId", schedule.getId());
        header.put("name", schedule.getName());
        header.put("date", date);
        header.put("time", schedule.getTimeText());
        header.put("room", schedule.getRoom());
        header.put("campusId", schedule.getCampusId());
        return buildRosterDetail(scheduleId, date, header);
    }

    @Transactional
    public Map<String, Object> manualStudentCheckin(Long teacherUserId, Long studentUserId, Long scheduleId, String classDate) {
        AppUser teacher = requireTeacherUser(teacherUserId);
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        if (!teacher.getTeacherId().equals(schedule.getTeacherId())) {
            throw new BizException("这不是你的课程，无法确认签到");
        }
        String date = normalizeDate(classDate);
        appUserRepo.findById(studentUserId).orElseThrow(() -> new BizException("学员不存在"));
        bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(studentUserId, scheduleId, date, STATUS_PENDING)
                .orElseThrow(() -> new BizException("该学员未预约本节课"));
        String operator = teacherRepo.findById(teacher.getTeacherId())
                .map(com.forget.academy.entity.Teacher::getName)
                .orElse(firstNonBlank(teacher.getNickname(), "老师"));
        Map<String, Object> result = checkinService.manualCheckin(studentUserId, scheduleId, date, operator);
        syncArchiveCounts(scheduleId, date);
        return result;
    }

    @Transactional
    public Map<String, Object> teacherCheckin(Long userId, String raw) {
        AppUser user = requireTeacherUser(userId);
        Map<String, String> session = checkinService.resolveSession(raw);
        Long scheduleId = Long.parseLong(session.get("id"));
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        if (!user.getTeacherId().equals(schedule.getTeacherId())) {
            throw new BizException("这不是你的课程，无法签到");
        }
        String classDate = session.get("date");
        ClassArchive archive = classArchiveRepo
                .findByTeacherIdAndScheduleIdAndClassDate(user.getTeacherId(), scheduleId, classDate)
                .orElseGet(ClassArchive::new);
        archive.setTeacherId(user.getTeacherId());
        archive.setScheduleId(scheduleId);
        archive.setClassDate(classDate);
        archive.setName(schedule.getName());
        archive.setTimeText(schedule.getTimeText());
        archive.setRoom(schedule.getRoom());
        archive.setCampusId(schedule.getCampusId() == null ? CampusIds.DEFAULT : schedule.getCampusId());
        archive.setDuration(session.get("duration"));
        archive.setTeacherCheckedAt(Instant.now());
        int booked = (int) bookingRepo.countByScheduleIdAndClassDateAndStatus(scheduleId, classDate, STATUS_PENDING);
        int checked = (int) practiceRecordRepo.countBySessionIdAndClassDate(String.valueOf(scheduleId), classDate);
        archive.setBookedCount(booked);
        archive.setCheckedInCount(checked);
        classArchiveRepo.save(archive);
        Map<String, Object> map = toArchiveMap(archive);
        map.put("message", schedule.getName() + " 课堂签到成功");
        return map;
    }

    public Map<String, Object> syncArchiveCounts(Long scheduleId, String classDate) {
        classArchiveRepo.findAll().stream()
                .filter(item -> scheduleId.equals(item.getScheduleId()) && classDate.equals(item.getClassDate()))
                .forEach(item -> {
                    item.setBookedCount((int) bookingRepo.countByScheduleIdAndClassDateAndStatus(
                            scheduleId, classDate, STATUS_PENDING));
                    item.setCheckedInCount((int) practiceRecordRepo.countBySessionIdAndClassDate(
                            String.valueOf(scheduleId), classDate));
                    classArchiveRepo.save(item);
                });
        return Map.of("ok", true);
    }

    private Map<String, Object> toScheduleRow(Schedule schedule, String classDate) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", schedule.getId());
        map.put("type", schedule.getType());
        map.put("name", schedule.getName());
        map.put("time", schedule.getTimeText());
        map.put("room", schedule.getRoom());
        map.put("campusId", schedule.getCampusId());
        map.put("date", classDate);
        map.put("capacity", schedule.getCapacity());
        long booked = bookingRepo.countByScheduleIdAndClassDateAndStatus(schedule.getId(), classDate, STATUS_PENDING);
        long checked = practiceRecordRepo.countBySessionIdAndClassDate(String.valueOf(schedule.getId()), classDate);
        map.put("bookedCount", booked);
        map.put("checkedInCount", checked);
        boolean teacherChecked = classArchiveRepo
                .findByTeacherIdAndScheduleIdAndClassDate(schedule.getTeacherId(), schedule.getId(), classDate)
                .map(item -> item.getTeacherCheckedAt() != null)
                .orElse(false);
        map.put("teacherChecked", teacherChecked);
        return map;
    }

    private Map<String, Object> toArchiveMap(ClassArchive archive) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", archive.getId());
        map.put("scheduleId", archive.getScheduleId());
        map.put("name", archive.getName());
        map.put("date", archive.getClassDate());
        map.put("time", archive.getTimeText());
        map.put("room", archive.getRoom());
        map.put("campusId", archive.getCampusId());
        map.put("duration", archive.getDuration());
        map.put("bookedCount", safeInt(archive.getBookedCount()));
        map.put("checkedInCount", safeInt(archive.getCheckedInCount()));
        map.put("teacherCheckedAt", archive.getTeacherCheckedAt() == null ? null : archive.getTeacherCheckedAt().toEpochMilli());
        map.put("studentFeedback", archive.getStudentFeedback());
        map.put("renewalRate", archive.getRenewalRate());
        map.put("note", archive.getNote());
        return map;
    }

    private Map<String, Object> buildRosterDetail(Long scheduleId, String classDate, Map<String, Object> header) {
        String sessionId = String.valueOf(scheduleId);
        List<Booking> bookings = bookingRepo.findByScheduleIdAndClassDateAndStatusOrderByIdAsc(
                scheduleId, classDate, STATUS_PENDING);
        List<PracticeRecord> checked = practiceRecordRepo.findBySessionIdAndClassDateOrderByCheckedAtAsc(
                sessionId, classDate);
        header.put("bookedCount", bookings.size());
        header.put("checkedInCount", checked.size());
        header.put("bookings", bookings.stream().map(booking -> toBookingMap(booking, sessionId, classDate)).toList());
        header.put("checkedIn", checked.stream().map(this::toPracticeMap).toList());
        return header;
    }

    private Map<String, Object> toBookingMap(Booking booking, String sessionId, String classDate) {
        Map<String, Object> map = toBookingMap(booking);
        PracticeRecord record = practiceRecordRepo
                .findByUserIdAndSessionIdAndClassDate(booking.getUserId(), sessionId, classDate)
                .orElse(null);
        map.put("checkedIn", record != null);
        checkinPendingRepo.findByUserIdAndScheduleIdAndClassDate(booking.getUserId(), booking.getScheduleId(), classDate)
                .ifPresent(pending -> {
                    map.put("checkinPending", CheckinPendingService.STATUS_PENDING.equals(pending.getStatus()));
                    map.put("checkinRejected", CheckinPendingService.STATUS_REJECTED.equals(pending.getStatus()));
                });
        if (record != null) {
            map.put("checkinSource", record.getCheckinSource());
            map.put("operatorName", record.getOperatorName());
            map.put("checkedAt", record.getCheckedAt() == null ? null : record.getCheckedAt().toEpochMilli());
        }
        return map;
    }

    private Map<String, Object> toBookingMap(Booking booking) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", booking.getId());
        map.put("userId", booking.getUserId());
        map.put("nickname", booking.getNickname());
        map.put("status", booking.getStatus());
        return map;
    }

    private Map<String, Object> toPracticeMap(PracticeRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("userId", record.getUserId());
        map.put("checkinSource", record.getCheckinSource());
        map.put("operatorName", record.getOperatorName());
        map.put("checkedAt", record.getCheckedAt() == null ? null : record.getCheckedAt().toEpochMilli());
        appUserRepo.findById(record.getUserId()).ifPresent(user -> map.put("nickname", user.getNickname()));
        return map;
    }

    private int sumMinutes(List<ClassArchive> archives) {
        return archives.stream()
                .filter(item -> item.getTeacherCheckedAt() != null)
                .mapToInt(item -> parseMinutes(item.getDuration()))
                .sum();
    }

    private int parseMinutes(String duration) {
        if (duration == null || duration.isBlank()) {
            return 75;
        }
        var matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(duration);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 75;
    }

    private String formatHours(int minutes) {
        if (minutes <= 0) {
            return "0";
        }
        double hours = minutes / 60.0;
        if (Math.abs(hours - Math.round(hours)) < 0.01) {
            return String.valueOf(Math.round(hours));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", hours);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int toWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SUNDAY ? 0 : day.getValue();
    }

    private String normalizeDate(String classDate) {
        return classDate == null || classDate.isBlank() ? LocalDate.now().toString() : classDate.trim();
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
