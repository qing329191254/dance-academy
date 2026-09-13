package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.ClassArchive;
import com.forget.academy.entity.Feedback;
import com.forget.academy.entity.Opportunity;
import com.forget.academy.entity.OpportunityApply;
import com.forget.academy.entity.PracticeRecord;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.ClassArchiveRepo;
import com.forget.academy.repo.ClassSessionCancelRepo;
import com.forget.academy.repo.FeedbackRepo;
import com.forget.academy.repo.OpportunityApplyRepo;
import com.forget.academy.repo.OpportunityRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.entity.TeacherAttendance;
import com.forget.academy.entity.EmployeeDutyRecord;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.service.AdminAccessService;
import com.forget.academy.service.CampusCatalogService;
import com.forget.academy.service.EmployeeService;
import com.forget.academy.service.BookingService;
import com.forget.academy.service.CheckinService;
import com.forget.academy.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOpsController {
    private final BookingRepo bookingRepo;
    private final BookingService bookingService;
    private final OpportunityRepo opportunityRepo;
    private final OpportunityApplyRepo applyRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final FeedbackRepo feedbackRepo;
    private final ClassArchiveRepo classArchiveRepo;
    private final TeacherRepo teacherRepo;
    private final TeacherAttendanceRepo teacherAttendanceRepo;
    private final EmployeeDutyRecordRepo employeeDutyRecordRepo;
    private final EmployeeService employeeService;
    private final AppUserRepo appUserRepo;
    private final AdminAccessService adminAccessService;
    private final CampusCatalogService campusCatalogService;
    private final ScheduleRepo scheduleRepo;
    private final ClassSessionCancelRepo classSessionCancelRepo;
    private final CheckinService checkinService;
    private final TeacherService teacherService;

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard(@RequestParam(required = false) String campusId) {
        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        Instant weekAgo = Instant.now().minusSeconds(7 * 24 * 3600L);
        boolean campusFiltered = campusId != null && !campusId.isBlank();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("campusFiltered", campusFiltered);
        data.put("userCount", campusFiltered
                ? appUserRepo.countStudentsActiveInCampuses(campuses)
                : appUserRepo.countStudents());
        data.put("bookingToday", bookingRepo.countByClassDateAndStatusInCampuses(today, "待上课", campuses));
        data.put("pendingApplies", campusFiltered
                ? applyRepo.countByStatusAndUserActiveInCampuses("pending", campuses)
                : applyRepo.countByStatus("pending"));
        data.put("practiceWeek", practiceRecordRepo.countByCheckedAtAfterAndCampusIdIn(weekAgo, campuses));
        data.put("latestBookings", bookingRepo.findLatestInCampuses(campuses, PageRequest.of(0, 8)).getContent()
                .stream()
                .map(this::toBookingRow)
                .toList());
        data.put("latestApplies", (campusFiltered
                ? applyRepo.findLatestByUserActiveInCampuses(campuses, PageRequest.of(0, 8))
                : applyRepo.findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id")))).getContent());
        data.put("campusIds", campuses);
        return ApiResponse.ok(data);
    }

    @GetMapping("/bookings")
    public ApiResponse<?> bookings(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "") String status,
                                  @RequestParam(defaultValue = "") String cancelSource,
                                  @RequestParam(required = false) Long scheduleId,
                                  @RequestParam(defaultValue = "") String classDate,
                                  @RequestParam(required = false) String campusId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        String query = keyword == null ? "" : keyword.trim();
        String st = status == null ? "" : status.trim();
        String source = cancelSource == null ? "" : cancelSource.trim();
        String date = classDate == null ? "" : classDate.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        var result = bookingRepo.searchInCampuses(query, st, source, scheduleId, date, campuses, pageable);
        var list = result.getContent().stream().map(this::toBookingRow).toList();
        return ApiResponse.ok(new PageResult<>(list, result.getTotalElements(), page, size));
    }

    /** 按上课日期汇总每节课预约人数（团课按星期匹配排课） */
    @GetMapping("/booking-sessions")
    public ApiResponse<?> bookingSessions(@RequestParam String date,
                                          @RequestParam(required = false) String campusId,
                                          @RequestParam(required = false) Long teacherId,
                                          @RequestParam(defaultValue = "") String keyword) {
        if (date == null || date.isBlank()) {
            throw new BizException("请选择上课日期");
        }
        LocalDate classDay;
        try {
            classDay = LocalDate.parse(date.trim());
        } catch (Exception e) {
            throw new BizException("上课日期格式不正确");
        }
        int weekday = toWeekday(classDay);
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        var campuses = adminAccessService.resolveCampusScope(campusId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (String campus : campuses) {
            for (Schedule schedule : scheduleRepo.findByCampusIdAndEnabledTrue(campus)) {
                if (schedule.getWeekday() == null || schedule.getWeekday() != weekday) {
                    continue;
                }
                if (teacherId != null && !teacherId.equals(schedule.getTeacherId())) {
                    continue;
                }
                if (!query.isEmpty()) {
                    String hay = ((schedule.getName() == null ? "" : schedule.getName()) + " "
                            + (schedule.getTeacherName() == null ? "" : schedule.getTeacherName()) + " "
                            + (schedule.getRoom() == null ? "" : schedule.getRoom())).toLowerCase(Locale.ROOT);
                    if (!hay.contains(query)) {
                        continue;
                    }
                }
                String classDate = classDay.toString();
                // 已约 = 待上课 + 已完成，避免确认到场后人数掉成 0
                int pending = (int) bookingRepo.countByScheduleIdAndClassDateAndStatus(
                        schedule.getId(), classDate, "待上课");
                int done = (int) bookingRepo.countByScheduleIdAndClassDateAndStatus(
                        schedule.getId(), classDate, "已完成");
                int booked = pending + done;
                int waitlisted = (int) bookingRepo.countByScheduleIdAndClassDateAndStatus(
                        schedule.getId(), classDate, "排队中");
                boolean cancelRecorded = classSessionCancelRepo.existsByScheduleIdAndClassDate(
                        schedule.getId(), classDate);
                // 已有人完成到场时不再展示「已取消」，避免课已上完仍显示取消
                boolean cancelled = cancelRecorded && done == 0;
                int capacity = schedule.getCapacity() == null ? 0 : schedule.getCapacity();
                Integer minEnrollment = schedule.getMinEnrollment();
                if (minEnrollment == null) {
                    minEnrollment = 4;
                }
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("scheduleId", schedule.getId());
                row.put("classDate", classDate);
                row.put("name", schedule.getName());
                row.put("type", schedule.getType());
                row.put("timeText", schedule.getTimeText());
                row.put("teacherId", schedule.getTeacherId());
                row.put("teacherName", schedule.getTeacherName());
                row.put("room", schedule.getRoom());
                row.put("campusId", schedule.getCampusId());
                row.put("campusName", campusCatalogService.displayName(schedule.getCampusId()));
                row.put("capacity", capacity);
                row.put("minEnrollment", minEnrollment);
                row.put("bookedCount", booked);
                row.put("pendingCount", pending);
                row.put("doneCount", done);
                row.put("waitlistCount", waitlisted);
                row.put("sessionCancelled", cancelled);
                row.put("full", capacity > 0 && booked >= capacity);
                rows.add(row);
            }
        }
        rows.sort(Comparator
                .comparing((Map<String, Object> r) -> String.valueOf(r.getOrDefault("timeText", "")))
                .thenComparing(r -> String.valueOf(r.getOrDefault("name", ""))));
        return ApiResponse.ok(rows);
    }

    private static int toWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SUNDAY ? 0 : day.getValue();
    }

    @PostMapping("/bookings")
    public ApiResponse<?> createBooking(@RequestBody Map<String, Object> body) {
        Long userId = parseLong(body.get("userId"));
        Long scheduleId = parseLong(body.get("scheduleId"));
        String classDate = body.get("classDate") == null ? "" : String.valueOf(body.get("classDate")).trim();
        if (userId == null || scheduleId == null) {
            throw new BizException("请选择学员和课程");
        }
        return ApiResponse.ok(bookingService.adminCreate(userId, scheduleId, classDate));
    }

    @PutMapping("/bookings/{id}")
    public ApiResponse<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking body) {
        return ApiResponse.ok(bookingService.adminUpdateStatus(id, body.getStatus()));
    }

    @PostMapping("/bookings/{id}/cancel")
    public ApiResponse<Booking> cancelBooking(@PathVariable Long id) {
        return ApiResponse.ok(bookingService.adminCancel(id));
    }

    @PostMapping("/booking-sessions/restore")
    public ApiResponse<?> restoreSession(@RequestBody Map<String, Object> body) {
        Long scheduleId = parseLong(body.get("scheduleId"));
        String classDate = body.get("classDate") == null ? "" : String.valueOf(body.get("classDate")).trim();
        return ApiResponse.ok(bookingService.restoreSession(scheduleId, classDate));
    }

    @GetMapping("/upcoming-students")
    public ApiResponse<?> upcomingStudents(@RequestParam(defaultValue = "7") String range,
                                           @RequestParam(required = false) String campusId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        String fromDate = today.toString();
        String toDate = "";
        String key = range == null ? "7" : range.trim().toLowerCase(Locale.ROOT);
        if ("15".equals(key)) {
            toDate = today.plusDays(15).toString();
        } else if ("30".equals(key)) {
            toDate = today.plusDays(30).toString();
        } else if ("future".equals(key) || "all".equals(key)) {
            toDate = "";
        } else {
            toDate = today.plusDays(7).toString();
            key = "7";
        }
        var campuses = adminAccessService.resolveCampusScope(campusId);
        List<Booking> bookings = bookingRepo.findUpcomingInCampuses(
                List.of("待上课", "排队中"), fromDate, toDate, campuses);
        Map<Long, Map<String, Object>> byUser = new LinkedHashMap<>();
        for (Booking booking : bookings) {
            if (booking.getUserId() == null) {
                continue;
            }
            Map<String, Object> row = byUser.computeIfAbsent(booking.getUserId(), id -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("userId", id);
                item.put("nickname", booking.getNickname());
                item.put("upcomingCount", 0);
                item.put("classes", new ArrayList<Map<String, Object>>());
                return item;
            });
            row.put("nickname", booking.getNickname() != null ? booking.getNickname() : row.get("nickname"));
            row.put("upcomingCount", ((Number) row.get("upcomingCount")).intValue() + 1);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> classes = (List<Map<String, Object>>) row.get("classes");
            Map<String, Object> cls = new LinkedHashMap<>();
            cls.put("bookingId", booking.getId());
            cls.put("name", booking.getName());
            cls.put("classDate", booking.getClassDate());
            cls.put("timeText", booking.getTimeText());
            cls.put("teacherName", booking.getTeacherName());
            cls.put("status", booking.getStatus());
            cls.put("room", booking.getRoom());
            scheduleRepo.findById(booking.getScheduleId()).ifPresent(schedule -> {
                cls.put("campusId", schedule.getCampusId());
                cls.put("campusName", campusCatalogService.displayName(schedule.getCampusId()));
            });
            classes.add(cls);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("range", key);
        data.put("fromDate", fromDate);
        data.put("toDate", toDate.isBlank() ? null : toDate);
        data.put("list", new ArrayList<>(byUser.values()));
        return ApiResponse.ok(data);
    }

    private Map<String, Object> toBookingRow(Booking booking) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", booking.getId());
        row.put("userId", booking.getUserId());
        row.put("nickname", booking.getNickname());
        row.put("scheduleId", booking.getScheduleId());
        row.put("name", booking.getName());
        row.put("classDate", booking.getClassDate());
        row.put("timeText", booking.getTimeText());
        row.put("teacherName", booking.getTeacherName());
        row.put("room", booking.getRoom());
        row.put("status", booking.getStatus());
        row.put("cancelSource", booking.getCancelSource());
        row.put("cancelSourceLabel", cancelSourceLabel(booking.getCancelSource(), booking.getStatus()));
        row.put("tab", booking.getTab());
        row.put("createdAt", booking.getCreatedAt());
        row.put("cardConsumed", Boolean.TRUE.equals(booking.getCardConsumed()));
        String sessionId = String.valueOf(booking.getScheduleId());
        String date = booking.getClassDate() == null ? "" : booking.getClassDate();
        // 已取消行不展示签到，避免同人同课同日改约后旧行误显示「已签到」
        boolean cancelled = "已取消".equals(booking.getStatus());
        row.put("checkedIn", !cancelled && practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(
                booking.getUserId(), sessionId, date));
        scheduleRepo.findById(booking.getScheduleId()).ifPresent(schedule -> {
            row.put("campusId", schedule.getCampusId());
            row.put("campusName", campusCatalogService.displayName(schedule.getCampusId()));
        });
        return row;
    }

    @DeleteMapping("/bookings/{id}")
    public ApiResponse<Void> deleteBooking(@PathVariable Long id) {
        bookingService.adminDelete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/checkin/manual")
    public ApiResponse<?> manualCheckin(@RequestBody Map<String, Object> body) {
        Long userId = parseLong(body.get("userId"));
        Long scheduleId = parseLong(body.get("scheduleId"));
        String classDate = body.get("classDate") == null ? "" : String.valueOf(body.get("classDate")).trim();
        if (userId == null || scheduleId == null || classDate.isBlank()) {
            throw new BizException("请填写学员、课表和上课日期");
        }
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        appUserRepo.findById(userId).orElseThrow(() -> new BizException("学员不存在"));
        bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(userId, scheduleId, classDate, "待上课")
                .orElseThrow(() -> new BizException("该学员未预约本节课"));
        var admin = adminAccessService.currentAdmin();
        String operator = admin.getName() == null || admin.getName().isBlank() ? admin.getUsername() : admin.getName();
        Map<String, Object> result = checkinService.manualCheckin(userId, scheduleId, classDate, operator);
        teacherService.syncArchiveCounts(scheduleId, classDate);
        return ApiResponse.ok(result);
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

    private static String cancelSourceLabel(String source, String status) {
        if (!"已取消".equals(status)) {
            return "";
        }
        if ("user".equals(source)) {
            return "自主取消";
        }
        if ("system_low_enrollment".equals(source)) {
            return "系统强制取消";
        }
        if ("admin".equals(source)) {
            return "后台取消";
        }
        return "已取消";
    }

    @GetMapping("/opportunities")
    public ApiResponse<?> opportunities(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(defaultValue = "") String keyword,
                                        @RequestParam(defaultValue = "") String trackKey,
                                        @RequestParam(required = false) Boolean enabled) {
        if (page == null) {
            return ApiResponse.ok(opportunityRepo.findAllByOrderByIdDesc());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id"));
        String query = keyword == null ? "" : keyword.trim();
        String track = trackKey == null ? "" : trackKey.trim();
        return ApiResponse.ok(PageResult.of(opportunityRepo.search(query, track, enabled, pageable)));
    }

    @PostMapping("/opportunities")
    public ApiResponse<Opportunity> createOpportunity(@RequestBody Opportunity body) {
        body.setId(null);
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        body.setCode(nextOpportunityCode(body.getTrackKey()));
        return ApiResponse.ok(opportunityRepo.save(body));
    }

    @PutMapping("/opportunities/{id}")
    public ApiResponse<Opportunity> updateOpportunity(@PathVariable Long id, @RequestBody Opportunity body) {
        Opportunity item = opportunityRepo.findById(id).orElseThrow(() -> new BizException("机会不存在"));
        item.setTrackKey(body.getTrackKey());
        if (item.getCode() == null || item.getCode().isBlank()) {
            item.setCode(nextOpportunityCode(body.getTrackKey()));
        }
        item.setTitle(body.getTitle());
        item.setDeadline(body.getDeadline());
        item.setSpots(body.getSpots());
        item.setLevel(body.getLevel());
        item.setSummary(body.getSummary());
        item.setEnabled(body.getEnabled());
        return ApiResponse.ok(opportunityRepo.save(item));
    }

    @DeleteMapping("/opportunities/{id}")
    public ApiResponse<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/applies")
    public ApiResponse<?> applies(@RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(defaultValue = "") String status,
                                 @RequestParam(required = false) String campusId,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        String query = keyword == null ? "" : keyword.trim();
        String st = status == null ? "" : status.trim();
        boolean campusFiltered = campusId != null && !campusId.isBlank();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        Page<OpportunityApply> result = campusFiltered
                ? applyRepo.searchInCampuses(query, st, campuses, pageable)
                : applyRepo.searchAll(query, st, pageable);
        return ApiResponse.ok(PageResult.of(result));
    }

    @PutMapping("/applies/{id}")
    public ApiResponse<OpportunityApply> updateApply(@PathVariable Long id, @RequestBody OpportunityApply body) {
        OpportunityApply apply = applyRepo.findById(id).orElseThrow(() -> new BizException("报名不存在"));
        if (body.getStatus() != null) {
            apply.setStatus(body.getStatus());
        }
        return ApiResponse.ok(applyRepo.save(apply));
    }

    @GetMapping("/practice")
    public ApiResponse<?> practice(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(required = false) String campusId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        String query = keyword == null ? "" : keyword.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        var result = practiceRecordRepo.searchInCampuses(query, campuses, pageable);
        var userIds = result.getContent().stream().map(PracticeRecord::getUserId).distinct().toList();
        var users = appUserRepo.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(AppUser::getId, u -> u.getNickname() == null ? "" : u.getNickname()));
        var list = result.getContent().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("userId", item.getUserId());
            row.put("nickname", users.getOrDefault(item.getUserId(), ""));
            row.put("name", item.getName());
            row.put("classDate", item.getClassDate());
            row.put("timeText", item.getTimeText());
            row.put("duration", item.getDuration());
            row.put("teacherName", item.getTeacherName());
            row.put("room", item.getRoom());
            row.put("campusId", item.getCampusId());
            row.put("checkinSource", item.getCheckinSource());
            row.put("operatorName", item.getOperatorName());
            row.put("checkedAt", item.getCheckedAt());
            return row;
        }).toList();
        return ApiResponse.ok(Map.of("list", list, "total", result.getTotalElements()));
    }

    @GetMapping("/feedbacks")
    public ApiResponse<?> feedbacks(@RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(required = false) String campusId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        String query = keyword == null ? "" : keyword.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        return ApiResponse.ok(PageResult.of(feedbackRepo.searchInCampuses(query, campuses, pageable)));
    }

    @GetMapping("/class-archives")
    public ApiResponse<?> classArchives(@RequestParam(defaultValue = "") String keyword,
                                        @RequestParam(required = false) Long teacherId,
                                        @RequestParam(required = false) String campusId,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        String query = keyword == null ? "" : keyword.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        Page<ClassArchive> result = classArchiveRepo.search(query, teacherId, campuses, pageable);
        var teachers = teacherRepo.findAllById(result.getContent().stream().map(ClassArchive::getTeacherId).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(com.forget.academy.entity.Teacher::getId, com.forget.academy.entity.Teacher::getName));
        var list = result.getContent().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("teacherId", item.getTeacherId());
            row.put("teacherName", teachers.getOrDefault(item.getTeacherId(), ""));
            row.put("scheduleId", item.getScheduleId());
            row.put("name", item.getName());
            row.put("classDate", item.getClassDate());
            row.put("timeText", item.getTimeText());
            row.put("room", item.getRoom());
            row.put("campusId", item.getCampusId());
            row.put("bookedCount", item.getBookedCount());
            row.put("checkedInCount", item.getCheckedInCount());
            row.put("teacherCheckedAt", item.getTeacherCheckedAt());
            row.put("studentFeedback", item.getStudentFeedback());
            row.put("renewalRate", item.getRenewalRate());
            return row;
        }).toList();
        return ApiResponse.ok(Map.of("list", list, "total", result.getTotalElements()));
    }

    @PutMapping("/class-archives/{id}")
    public ApiResponse<ClassArchive> updateClassArchive(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ClassArchive archive = classArchiveRepo.findById(id).orElseThrow(() -> new BizException("课堂档案不存在"));
        adminAccessService.assertCanAccessCampus(archive.getCampusId());
        if (body.get("studentFeedback") != null) {
            archive.setStudentFeedback(body.get("studentFeedback"));
        }
        if (body.get("renewalRate") != null) {
            archive.setRenewalRate(body.get("renewalRate"));
        }
        if (body.get("note") != null) {
            archive.setNote(body.get("note"));
        }
        return ApiResponse.ok(classArchiveRepo.save(archive));
    }

    @GetMapping("/teacher-attendance")
    public ApiResponse<?> teacherAttendance(@RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(required = false) String campusId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        var campuses = adminAccessService.resolveCampusScope(campusId);
        var result = teacherAttendanceRepo.searchInCampuses(keyword == null ? "" : keyword.trim(), campuses, pageable);
        var userIds = result.getContent().stream().map(TeacherAttendance::getUserId).distinct().toList();
        var users = appUserRepo.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(AppUser::getId, AppUser::getNickname));
        var list = result.getContent().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("nickname", users.getOrDefault(item.getUserId(), ""));
            row.put("className", item.getClassName());
            row.put("classDate", item.getClassDate());
            row.put("timeText", item.getTimeText());
            row.put("campusId", item.getCampusId());
            row.put("status", item.getStatus());
            row.put("lateMinutes", item.getLateMinutes());
            row.put("checkedAt", item.getCheckedAt());
            return row;
        }).toList();
        return ApiResponse.ok(Map.of("list", list, "total", result.getTotalElements()));
    }

    @GetMapping("/employee-duty")
    public ApiResponse<?> employeeDuty(@RequestParam(defaultValue = "") String keyword,
                                       @RequestParam(required = false) String campusId,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        var campuses = adminAccessService.resolveCampusScope(campusId);
        var result = employeeDutyRecordRepo.searchInCampuses(keyword == null ? "" : keyword.trim(), campuses, pageable);
        var userIds = result.getContent().stream().map(EmployeeDutyRecord::getUserId).distinct().toList();
        var users = appUserRepo.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(AppUser::getId, AppUser::getNickname));
        var list = result.getContent().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("nickname", users.getOrDefault(item.getUserId(), ""));
            row.put("className", item.getClassName());
            row.put("classDate", item.getClassDate());
            row.put("timeText", item.getTimeText());
            row.put("campusId", item.getCampusId());
            row.put("status", item.getStatus());
            row.put("lateMinutes", item.getLateMinutes());
            row.put("checkedAt", item.getCheckedAt());
            return row;
        }).toList();
        return ApiResponse.ok(Map.of("list", list, "total", result.getTotalElements()));
    }

    @PostMapping("/employees/{userId}/performance")
    public ApiResponse<?> publishEmployeePerformance(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(employeeService.publishPerformance(userId, body.get("periodLabel"), body.get("content")));
    }

    private String nextOpportunityCode(String trackKey) {
        String prefix = switch (trackKey == null ? "" : trackKey) {
            case "show", "commercial", "teacher" -> "d";
            default -> "w";
        };
        int seq = 1;
        while (opportunityRepo.findByCode(prefix + seq).isPresent()) {
            seq++;
        }
        return prefix + seq;
    }
}
