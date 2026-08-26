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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
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
    private final ScheduleRepo scheduleRepo;
    private final CheckinService checkinService;
    private final TeacherService teacherService;

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard(@RequestParam(required = false) String campusId) {
        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        Instant weekAgo = Instant.now().minusSeconds(7 * 24 * 3600L);
        var campuses = adminAccessService.resolveCampusScope(campusId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", appUserRepo.count());
        data.put("bookingToday", bookingRepo.countByClassDateAndStatusInCampuses(today, "待上课", campuses));
        data.put("pendingApplies", applyRepo.countByStatus("pending"));
        data.put("practiceWeek", practiceRecordRepo.countByCheckedAtAfterAndCampusIdIn(weekAgo, campuses));
        data.put("latestBookings", bookingRepo.findLatestInCampuses(campuses, PageRequest.of(0, 8)).getContent());
        data.put("latestApplies", applyRepo.findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        data.put("campusIds", campuses);
        return ApiResponse.ok(data);
    }

    @GetMapping("/bookings")
    public ApiResponse<?> bookings(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "") String status,
                                  @RequestParam(required = false) String campusId,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        String query = keyword == null ? "" : keyword.trim();
        String st = status == null ? "" : status.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        return ApiResponse.ok(PageResult.of(bookingRepo.searchInCampuses(query, st, campuses, pageable)));
    }

    @PutMapping("/bookings/{id}")
    public ApiResponse<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking body) {
        return ApiResponse.ok(bookingService.adminUpdateStatus(id, body.getStatus()));
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
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        Page<OpportunityApply> result;
        if (status != null && !status.isBlank()) {
            result = applyRepo.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = applyRepo.findByTitleContainingOrNicknameContaining(keyword, keyword, pageable);
        } else {
            result = applyRepo.findAll(pageable);
        }
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
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        String query = keyword == null ? "" : keyword.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        return ApiResponse.ok(PageResult.of(practiceRecordRepo.searchInCampuses(query, campuses, pageable)));
    }

    @GetMapping("/feedbacks")
    public ApiResponse<?> feedbacks(@RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(required = false) String campusId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
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
