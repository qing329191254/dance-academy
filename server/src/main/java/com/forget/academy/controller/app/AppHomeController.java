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
import com.forget.academy.repo.StudioRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.BookingService;
import com.forget.academy.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
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

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("studio", studio());
        data.put("banners", bannerRepo.findByEnabledTrueOrderBySortOrderAscIdAsc().stream().map(Banner::getImageUrl).toList());
        data.put("teachers", teacherRepo.findByEnabledTrueOrderBySortOrderAscIdAsc());
        data.put("courses", courseRepo.findByEnabledTrueOrderBySortOrderAscIdAsc().stream().map(this::toCourse).toList());
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
        return ApiResponse.ok(courseRepo.findByEnabledTrueOrderBySortOrderAscIdAsc().stream().map(this::toCourse).toList());
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<?> course(@PathVariable Long id) {
        return ApiResponse.ok(courseRepo.findById(id).map(this::toCourse).orElse(null));
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

    private Studio studio() {
        return studioRepo.findAll().stream().findFirst().orElseGet(Studio::new);
    }

    private Map<String, Object> toCourse(Course course) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", course.getId());
        map.put("name", course.getName());
        map.put("price", course.getPrice());
        map.put("level", course.getLevel());
        map.put("desc", course.getDescription());
        map.put("cover", course.getCover());
        return map;
    }
}
