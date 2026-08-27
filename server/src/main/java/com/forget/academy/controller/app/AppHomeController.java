package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.entity.Banner;
import com.forget.academy.entity.BrandPhoto;
import com.forget.academy.entity.Course;
import com.forget.academy.entity.Studio;
import com.forget.academy.entity.Teacher;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BrandPhotoRepo;
import com.forget.academy.repo.CourseRepo;
import com.forget.academy.repo.SchoolRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.BookingService;
import com.forget.academy.service.CourseModuleMapper;
import com.forget.academy.service.CampusContentService;
import com.forget.academy.service.GrowthContentService;
import com.forget.academy.service.LeaderboardService;
import com.forget.academy.service.StudioService;
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
    private final StudioService studioService;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;
    private final TeacherRepo teacherRepo;
    private final CourseRepo courseRepo;
    private final BookingService bookingService;
    private final LeaderboardService leaderboardService;
    private final SchoolRepo schoolRepo;
    private final GrowthContentService growthContentService;
    private final CampusContentService campusContentService;

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(@RequestParam(required = false) String campusId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studio", studio(campusId));
        data.put("banners", bannerRepo.findByCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(
                campusContentService.normalizeCampusId(campusId)).stream().map(Banner::getImageUrl).toList());
        data.put("teachers", teacherRepo.findByEnabledTrueOrderBySortOrderAscIdAsc());
        return ApiResponse.ok(data);
    }

    @GetMapping("/studio")
    public ApiResponse<Studio> studioInfo(@RequestParam(required = false) String campusId) {
        return ApiResponse.ok(studio(campusId));
    }

    @GetMapping("/brand")
    public ApiResponse<Map<String, Object>> brand(@RequestParam(required = false) String campusId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studio", studio(campusId));
        data.put("photos", brandPhotoRepo.findByCampusIdOrderBySortOrderAscIdAsc(
                campusContentService.normalizeCampusId(campusId)).stream().map(BrandPhoto::getImageUrl).toList());
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

    @GetMapping("/course-intro")
    public ApiResponse<Map<String, Object>> courseIntro(@RequestParam(required = false) String campusId) {
        Studio studio = studio(campusId);
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

    @GetMapping("/growth-content")
    public ApiResponse<Map<String, Object>> growthContent(@RequestParam(required = false) String campusId) {
        return ApiResponse.ok(growthContentService.content(campusId));
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

    private Studio studio(String campusId) {
        return studioService.resolveForApp(campusId);
    }
}
