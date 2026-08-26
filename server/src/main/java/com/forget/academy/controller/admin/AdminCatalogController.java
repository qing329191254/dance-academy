package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.Course;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.Teacher;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CourseRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.service.AdminAccessService;
import com.forget.academy.service.CheckinService;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {
    private final TeacherRepo teacherRepo;
    private final CourseRepo courseRepo;
    private final ScheduleRepo scheduleRepo;
    private final CheckinService checkinService;
    private final BookingRepo bookingRepo;
    private final AdminAccessService adminAccessService;

    @GetMapping("/teachers")
    public ApiResponse<?> teachers(@RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer size,
                                   @RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(required = false) Boolean enabled) {
        if (page == null) {
            return ApiResponse.ok(teacherRepo.findAllByOrderBySortOrderAscIdAsc());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()));
        String query = keyword == null ? "" : keyword.trim();
        return ApiResponse.ok(PageResult.of(teacherRepo.search(query, enabled, pageable)));
    }

    @PostMapping("/teachers")
    public ApiResponse<Teacher> createTeacher(@RequestBody Teacher body) {
        body.setId(null);
        defaults(body);
        return ApiResponse.ok(teacherRepo.save(body));
    }

    @PutMapping("/teachers/{id}")
    public ApiResponse<Teacher> updateTeacher(@PathVariable Long id, @RequestBody Teacher body) {
        Teacher teacher = teacherRepo.findById(id).orElseThrow(() -> new BizException("老师不存在"));
        teacher.setName(body.getName());
        teacher.setStyle(body.getStyle());
        teacher.setIntro(body.getIntro());
        teacher.setAvatar(body.getAvatar());
        teacher.setSortOrder(body.getSortOrder());
        teacher.setEnabled(body.getEnabled());
        return ApiResponse.ok(teacherRepo.save(teacher));
    }

    @DeleteMapping("/teachers/{id}")
    public ApiResponse<Void> deleteTeacher(@PathVariable Long id) {
        teacherRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/courses")
    public ApiResponse<?> courses(@RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer size,
                                  @RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(required = false) Boolean enabled) {
        if (page == null) {
            return ApiResponse.ok(courseRepo.findAllByOrderBySortOrderAscIdAsc());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()));
        String query = keyword == null ? "" : keyword.trim();
        return ApiResponse.ok(PageResult.of(courseRepo.search(query, enabled, pageable)));
    }

    @PostMapping("/courses")
    public ApiResponse<Course> createCourse(@RequestBody Course body) {
        body.setId(null);
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
        return ApiResponse.ok(courseRepo.save(body));
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<Course> updateCourse(@PathVariable Long id, @RequestBody Course body) {
        Course course = courseRepo.findById(id).orElseThrow(() -> new BizException("课程不存在"));
        course.setName(body.getName());
        course.setPrice(body.getPrice());
        course.setLevel(body.getLevel());
        course.setDescription(body.getDescription());
        course.setCover(body.getCover());
        course.setSortOrder(body.getSortOrder());
        course.setEnabled(body.getEnabled());
        return ApiResponse.ok(courseRepo.save(course));
    }

    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        courseRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/schedules")
    public ApiResponse<?> schedules(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size,
                                    @RequestParam(defaultValue = "") String keyword,
                                    @RequestParam(defaultValue = "") String type,
                                    @RequestParam(defaultValue = "") String campusId,
                                    @RequestParam(required = false) Boolean enabled) {
        if (page == null) {
            var campuses = adminAccessService.resolveCampusScope(campusId);
            return ApiResponse.ok(scheduleRepo.findAllByOrderByTypeAscSortOrderAscIdAsc().stream()
                    .filter(item -> campuses.contains(item.getCampusId()))
                    .toList());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by("type").ascending().and(Sort.by("sortOrder").ascending()).and(Sort.by("id").ascending()));
        String query = keyword == null ? "" : keyword.trim();
        String scheduleType = type == null ? "" : type.trim();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        return ApiResponse.ok(PageResult.of(scheduleRepo.search(query, scheduleType, campuses, enabled, pageable)));
    }

    @PostMapping("/schedules")
    public ApiResponse<Schedule> createSchedule(@RequestBody Schedule body) {
        body.setId(null);
        fillTeacherName(body);
        if (body.getCampusId() == null || body.getCampusId().isBlank()) {
            body.setCampusId(CampusIds.DEFAULT);
        }
        adminAccessService.assertCanAccessCampus(body.getCampusId());
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        if (body.getStars() == null) {
            body.setStars(3);
        }
        if (body.getCapacity() == null) {
            body.setCapacity(20);
        }
        return ApiResponse.ok(scheduleRepo.save(body));
    }

    @PutMapping("/schedules/{id}")
    public ApiResponse<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule body) {
        Schedule schedule = scheduleRepo.findById(id).orElseThrow(() -> new BizException("课表不存在"));
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        schedule.setType(body.getType());
        schedule.setCampusId(body.getCampusId() == null || body.getCampusId().isBlank()
                ? CampusIds.DEFAULT : body.getCampusId().trim());
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        schedule.setName(body.getName());
        schedule.setTimeText(body.getTimeText());
        schedule.setTeacherId(body.getTeacherId());
        schedule.setTeacherName(body.getTeacherName());
        schedule.setRoom(body.getRoom());
        schedule.setStars(body.getStars());
        schedule.setStatus(body.getStatus());
        schedule.setWeekday(body.getWeekday());
        schedule.setCapacity(body.getCapacity());
        schedule.setSortOrder(body.getSortOrder());
        schedule.setEnabled(body.getEnabled());
        fillTeacherName(schedule);
        return ApiResponse.ok(scheduleRepo.save(schedule));
    }

    @DeleteMapping("/schedules/{id}")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        Schedule schedule = scheduleRepo.findById(id).orElseThrow(() -> new BizException("课表不存在"));
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        scheduleRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/schedules/{id}/pending-count")
    public ApiResponse<?> pendingCount(@PathVariable Long id) {
        Schedule schedule = scheduleRepo.findById(id).orElseThrow(() -> new BizException("课表不存在"));
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        return ApiResponse.ok(java.util.Map.of(
                "count", bookingRepo.countByScheduleIdAndStatus(id, "待上课")));
    }

    @GetMapping("/schedules/{id}/checkin-payload")
    public ApiResponse<?> checkinPayload(@PathVariable Long id, @RequestParam(required = false) String date) {
        Schedule schedule = scheduleRepo.findById(id).orElseThrow(() -> new BizException("课表不存在"));
        adminAccessService.assertCanAccessCampus(schedule.getCampusId());
        return ApiResponse.ok(checkinService.payloadForSchedule(id, date));
    }

    private void defaults(Teacher body) {
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
    }

    private void fillTeacherName(Schedule body) {
        if ((body.getTeacherName() == null || body.getTeacherName().isBlank()) && body.getTeacherId() != null) {
            teacherRepo.findById(body.getTeacherId()).ifPresent(t -> body.setTeacherName(t.getName()));
        }
    }
}
