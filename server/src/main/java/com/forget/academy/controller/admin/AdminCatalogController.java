package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.common.CourseModuleTypes;
import com.forget.academy.common.ClosedClassGroup;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Course;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.Teacher;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.CourseRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.service.AdminAccessService;
import com.forget.academy.service.CourseModuleMapper;
import com.forget.academy.service.TeacherResumeService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {
    private final TeacherRepo teacherRepo;
    private final AppUserRepo appUserRepo;
    private final CourseRepo courseRepo;
    private final ScheduleRepo scheduleRepo;
    private final BookingRepo bookingRepo;
    private final AdminAccessService adminAccessService;
    private final TeacherResumeService teacherResumeService;

    @GetMapping("/teachers")
    public ApiResponse<?> teachers(@RequestParam(required = false) Integer page,
                                   @RequestParam(required = false) Integer size,
                                   @RequestParam(defaultValue = "") String keyword,
                                   @RequestParam(required = false) Boolean enabled,
                                   @RequestParam(required = false) String campusId) {
        if (page == null) {
            return ApiResponse.ok(teacherRepo.findAllByOrderBySortOrderAscIdAsc());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()));
        String query = keyword == null ? "" : keyword.trim();
        boolean campusFiltered = campusId != null && !campusId.isBlank();
        var campuses = adminAccessService.resolveCampusScope(campusId);
        var pageResult = campusFiltered
                ? teacherRepo.searchInCampuses(query, enabled, campuses, pageable)
                : teacherRepo.search(query, enabled, pageable);
        return ApiResponse.ok(new PageResult<>(
                enrichTeachers(pageResult.getContent()),
                pageResult.getTotalElements(),
                pageResult.getNumber() + 1,
                pageResult.getSize()));
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
        teacherResumeService.deleteByTeacherId(id);
        teacherRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/teachers/{id}/resume")
    public ApiResponse<?> teacherResume(@PathVariable Long id) {
        return ApiResponse.ok(teacherResumeService.adminResume(id));
    }

    @GetMapping("/courses")
    public ApiResponse<?> courses(@RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer size,
                                  @RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(required = false) Boolean enabled,
                                  @RequestParam(defaultValue = "") String moduleType) {
        if (page == null) {
            return ApiResponse.ok(courseRepo.findAllByOrderBySortOrderAscIdAsc());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(
                Math.max(page - 1, 0),
                pageSize,
                Sort.by("sortOrder").ascending().and(Sort.by("id").ascending()));
        String query = keyword == null ? "" : keyword.trim();
        String type = moduleType == null ? "" : moduleType.trim();
        return ApiResponse.ok(PageResult.of(courseRepo.search(query, enabled, type, pageable)));
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
        normalizeCourseModule(body);
        return ApiResponse.ok(courseRepo.save(body));
    }

    @PutMapping("/courses/{id}")
    public ApiResponse<Course> updateCourse(@PathVariable Long id, @RequestBody Course body) {
        Course course = courseRepo.findById(id).orElseThrow(() -> new BizException("课程不存在"));
        applyCourseFields(course, body);
        normalizeCourseModule(course);
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
        normalizeClosedDoor(body);
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
        schedule.setClosedDoor(body.getClosedDoor());
        schedule.setAudienceGroup(body.getAudienceGroup());
        normalizeClosedDoor(schedule);
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

    private void defaults(Teacher body) {
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
    }

    private void applyCourseFields(Course course, Course body) {
        course.setName(body.getName());
        course.setPrice(body.getPrice());
        course.setPriceDisplay(body.getPriceDisplay());
        course.setPriceUnit(body.getPriceUnit());
        course.setLevel(body.getLevel());
        course.setDescription(body.getDescription());
        course.setSummary(body.getSummary());
        course.setTag(body.getTag());
        course.setModuleType(body.getModuleType());
        course.setModuleKey(body.getModuleKey());
        course.setHighlights(body.getHighlights());
        course.setActionLabel(body.getActionLabel());
        course.setActionTab(body.getActionTab());
        course.setCover(body.getCover());
        course.setSortOrder(body.getSortOrder());
        course.setEnabled(body.getEnabled());
    }

    private void normalizeCourseModule(Course course) {
        if (course.getModuleType() == null || course.getModuleType().isBlank()) {
            course.setModuleType(CourseModuleTypes.SYSTEM);
        }
        if (!CourseModuleTypes.isValid(course.getModuleType())) {
            throw new BizException("模块类型无效");
        }
        if (course.getPriceUnit() == null || course.getPriceUnit().isBlank()) {
            course.setPriceUnit("节");
        }
        if (CourseModuleTypes.SYSTEM.equals(course.getModuleType())
                && (course.getModuleKey() == null || course.getModuleKey().isBlank())) {
            throw new BizException("课程产品需填写模块标识");
        }
    }

    private void fillTeacherName(Schedule body) {
        if ((body.getTeacherName() == null || body.getTeacherName().isBlank()) && body.getTeacherId() != null) {
            teacherRepo.findById(body.getTeacherId()).ifPresent(t -> body.setTeacherName(t.getName()));
        }
    }

    private void normalizeClosedDoor(Schedule schedule) {
        if (!"group".equals(schedule.getType())) {
            schedule.setClosedDoor(false);
            schedule.setAudienceGroup(null);
            return;
        }
        if (!Boolean.TRUE.equals(schedule.getClosedDoor())) {
            schedule.setClosedDoor(false);
            schedule.setAudienceGroup(null);
            return;
        }
        String audience = schedule.getAudienceGroup() == null ? "" : schedule.getAudienceGroup().trim();
        if (!ClosedClassGroup.isValid(audience)) {
            throw new BizException("闭门课需选择面向分组：高阶闭门或零基础闭门");
        }
        schedule.setAudienceGroup(audience);
    }

    private List<Map<String, Object>> enrichTeachers(List<Teacher> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return List.of();
        }
        var teacherIds = teachers.stream().map(Teacher::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AppUser> boundUsers = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            for (AppUser user : appUserRepo.findByTeacherIdIn(teacherIds)) {
                if (user.getTeacherId() != null) {
                    boundUsers.putIfAbsent(user.getTeacherId(), user);
                }
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>(teachers.size());
        for (Teacher teacher : teachers) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", teacher.getId());
            row.put("name", teacher.getName());
            row.put("style", teacher.getStyle());
            row.put("intro", teacher.getIntro());
            row.put("avatar", teacher.getAvatar());
            row.put("sortOrder", teacher.getSortOrder());
            row.put("enabled", teacher.getEnabled());
            AppUser bound = boundUsers.get(teacher.getId());
            row.put("boundAccountNickname", bound == null ? null : bound.getNickname());
            row.put("boundUserId", bound == null ? null : bound.getId());
            Map<String, Object> resume = teacherResumeService.summary(teacher.getId());
            row.put("hasResume", resume.get("hasResume"));
            row.put("resumePhotoCount", resume.get("photoCount"));
            row.put("resumeVideoCount", resume.get("videoCount"));
            rows.add(row);
        }
        return rows;
    }
}
