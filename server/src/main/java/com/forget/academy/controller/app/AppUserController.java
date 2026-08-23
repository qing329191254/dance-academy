package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.entity.AppUser;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.UserCardRepo;
import com.forget.academy.repo.UserCourseRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.AppAuthService;
import com.forget.academy.service.BookingService;
import com.forget.academy.service.CheckinService;
import com.forget.academy.service.GrowthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppUserController {
    private final AppAuthService appAuthService;
    private final BookingService bookingService;
    private final CheckinService checkinService;
    private final GrowthService growthService;
    private final AppUserRepo appUserRepo;
    private final UserCardRepo userCardRepo;
    private final UserCourseRepo userCourseRepo;

    @PostMapping("/auth/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(appAuthService.login(body.get("code")));
    }

    @GetMapping("/auth/profile")
    public ApiResponse<?> profile() {
        return ApiResponse.ok(appAuthService.me(AuthContext.requireApp().id()));
    }

    @PostMapping("/auth/profile")
    public ApiResponse<?> saveProfile(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(appAuthService.completeProfile(AuthContext.requireApp().id(), body));
    }

    @GetMapping("/mine")
    public ApiResponse<?> mine() {
        Long userId = AuthContext.requireApp().id();
        AppUser user = appUserRepo.findById(userId).orElseThrow();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", AppAuthService.toUserMap(user));
        data.put("growth", growthService.overview(user));
        data.put("cards", userCardRepo.findByUserIdOrderByIdDesc(userId));
        data.put("courses", userCourseRepo.findByUserIdOrderByIdDesc(userId));
        data.put("bookings", bookingService.myBookings(userId));
        data.put("practice", checkinService.myPractice(userId));
        return ApiResponse.ok(data);
    }

    @GetMapping("/cards")
    public ApiResponse<?> cards() {
        return ApiResponse.ok(userCardRepo.findByUserIdOrderByIdDesc(AuthContext.requireApp().id())
                .stream()
                .map(card -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", card.getId());
                    map.put("name", card.getName());
                    map.put("type", card.getType());
                    map.put("remain", card.getRemain());
                    map.put("total", card.getTotal());
                    map.put("expire", card.getExpireDate());
                    map.put("cover", card.getCover());
                    return map;
                })
                .toList());
    }

    @GetMapping("/my-courses")
    public ApiResponse<?> myCourses() {
        return ApiResponse.ok(userCourseRepo.findByUserIdOrderByIdDesc(AuthContext.requireApp().id())
                .stream()
                .map(item -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", item.getId());
                    map.put("name", item.getName());
                    map.put("teacher", item.getTeacherName());
                    map.put("progress", item.getProgress());
                    map.put("status", item.getStatus());
                    return map;
                })
                .toList());
    }

    @GetMapping("/bookings")
    public ApiResponse<?> bookings() {
        return ApiResponse.ok(bookingService.myBookings(AuthContext.requireApp().id()));
    }

    @PostMapping("/bookings")
    public ApiResponse<?> toggleBooking(@RequestBody Map<String, Object> body) {
        Long scheduleId = Long.valueOf(String.valueOf(body.get("scheduleId")));
        String date = body.get("date") == null ? null : String.valueOf(body.get("date"));
        return ApiResponse.ok(bookingService.toggle(AuthContext.requireApp().id(), scheduleId, date));
    }

    @GetMapping("/practice")
    public ApiResponse<?> practice() {
        return ApiResponse.ok(checkinService.myPractice(AuthContext.requireApp().id()));
    }

    @PostMapping("/checkin")
    public ApiResponse<?> checkin(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(checkinService.checkin(AuthContext.requireApp().id(), body.get("payload")));
    }

    @GetMapping("/growth")
    public ApiResponse<?> growth() {
        AppUser user = appUserRepo.findById(AuthContext.requireApp().id()).orElseThrow();
        return ApiResponse.ok(growthService.overview(user));
    }

    @GetMapping("/opportunities")
    public ApiResponse<?> opportunities(@RequestParam String trackKey) {
        Long userId = AuthContext.get() != null && AuthContext.ROLE_APP.equals(AuthContext.get().role())
                ? AuthContext.get().id() : null;
        return ApiResponse.ok(growthService.listByTrack(trackKey, userId));
    }

    @PostMapping("/opportunities/{id}/apply")
    public ApiResponse<?> apply(@org.springframework.web.bind.annotation.PathVariable Long id) {
        return ApiResponse.ok(growthService.toggleApply(AuthContext.requireApp().id(), id));
    }
}
