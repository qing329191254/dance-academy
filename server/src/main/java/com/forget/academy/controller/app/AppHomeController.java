package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.CourseModuleTypes;
import com.forget.academy.entity.Banner;
import com.forget.academy.entity.BrandPhoto;
import com.forget.academy.entity.Course;
import com.forget.academy.entity.Studio;
import com.forget.academy.entity.Teacher;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BrandPhotoRepo;
import com.forget.academy.repo.CourseRepo;
import com.forget.academy.repo.SchoolRepo;
import com.forget.academy.repo.StudioRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.BookingService;
import com.forget.academy.service.CourseModuleMapper;
import com.forget.academy.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppHomeController {
    private final StudioRepo studioRepo;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;
    private final TeacherRepo teacherRepo;
    private final CourseRepo courseRepo;
    private final BookingService bookingService;
    private final LeaderboardService leaderboardService;
    private final SchoolRepo schoolRepo;

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studio", studio());
        data.put("banners", bannerRepo.findByEnabledTrueOrderBySortOrderAscIdAsc().stream().map(Banner::getImageUrl).toList());
        data.put("teachers", teacherRepo.findByEnabledTrueOrderBySortOrderAscIdAsc());
        data.put("courses", productCourses());
        return ApiResponse.ok(data);
    }

    @GetMapping("/studio")
    public ApiResponse<Studio> studioInfo() {
        return ApiResponse.ok(studio());
    }

    @GetMapping("/brand")
    public ApiResponse<Map<String, Object>> brand() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studio", studio());
        data.put("photos", brandPhotoRepo.findAllByOrderBySortOrderAscIdAsc().stream().map(BrandPhoto::getImageUrl).toList());
        return ApiResponse.ok(data);
    }

    @GetMapping("/teachers")
    public ApiResponse<?> teachers() {
        return ApiResponse.ok(teacherRepo.findByEnabledTrueOrderBySortOrderAscIdAsc());
    }

    @GetMapping("/teachers/{id}")
    public ApiResponse<Teacher> teacher(@PathVariable Long id) {
        return ApiResponse.ok(teacherRepo.findById(id).orElse(null));
    }

    @GetMapping("/courses")
    public ApiResponse<?> courses() {
        return ApiResponse.ok(productCourses());
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<?> course(@PathVariable Long id) {
        return ApiResponse.ok(courseRepo.findById(id).map(this::toCourse).orElse(null));
    }

    @GetMapping("/course-intro")
    public ApiResponse<Map<String, Object>> courseIntro() {
        Studio studio = studio();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("trial", courseRepo.findByModuleTypeAndEnabledTrueOrderBySortOrderAscIdAsc("trial").stream()
                .findFirst()
                .map(CourseModuleMapper::toModuleMap)
                .orElse(null));
        data.put("systemModules", courseRepo.findByModuleTypeAndEnabledTrueOrderBySortOrderAscIdAsc("system").stream()
                .map(CourseModuleMapper::toModuleMap)
                .toList());
        data.put("systemLead", studio.getCourseSystemLead());
        data.put("systemHomeSummary", studio.getCourseSystemHomeSummary());
        return ApiResponse.ok(data);
    }

    @GetMapping("/course-modules/{id}")
    public ApiResponse<?> courseModule(@PathVariable Long id) {
        return ApiResponse.ok(courseRepo.findById(id)
                .filter(course -> Boolean.TRUE.equals(course.getEnabled()))
                .map(CourseModuleMapper::toModuleMap)
                .orElse(null));
    }

    @GetMapping("/schedules")
    public ApiResponse<?> schedules(@RequestParam(defaultValue = "group") String type,
                                   @RequestParam(required = false) String date,
                                   @RequestParam(required = false) String campusId) {
        Long userId = AuthContext.get() == null ? null : AuthContext.get().id();
        return ApiResponse.ok(bookingService.listSchedules(type, date, campusId, userId));
    }

    @GetMapping("/leaderboard")
    public ApiResponse<?> leaderboard(@RequestParam(defaultValue = "month") String period,
                                     @RequestParam(required = false) String campusId) {
        Long userId = AuthContext.get() == null ? null : AuthContext.get().id();
        return ApiResponse.ok(leaderboardService.list(period, campusId, userId));
    }

    @GetMapping("/schools")
    public ApiResponse<?> schools() {
        return ApiResponse.ok(schoolRepo.findByEnabledTrueOrderBySortOrderAscIdAsc());
    }

    private Studio studio() {
        return studioRepo.findAll().stream().findFirst().orElseGet(Studio::new);
    }

    private List<Map<String, Object>> productCourses() {
        return courseRepo.findByEnabledTrueOrderBySortOrderAscIdAsc().stream()
                .filter(this::isProductCourse)
                .map(this::toCourse)
                .toList();
    }

    private boolean isProductCourse(Course course) {
        String type = course.getModuleType();
        return type == null || type.isBlank() || CourseModuleTypes.PRODUCT.equals(type);
    }

    private Map<String, Object> toCourse(Course course) {
        Map<String, Object> map = CourseModuleMapper.toModuleMap(course);
        map.put("desc", course.getDescription());
        return map;
    }
}
