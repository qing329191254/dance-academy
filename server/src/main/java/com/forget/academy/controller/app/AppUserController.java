package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Feedback;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.FeedbackRepo;
import com.forget.academy.repo.UserCardRepo;
import com.forget.academy.repo.UserCourseRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.AppAuthService;
import com.forget.academy.service.BookingService;
import com.forget.academy.service.CheckinPendingService;
import com.forget.academy.service.CheckinService;
import com.forget.academy.service.GrowthService;
import com.forget.academy.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppUserController {
    private final AppAuthService appAuthService;
    private final BookingService bookingService;
    private final CheckinService checkinService;
    private final CheckinPendingService checkinPendingService;
    private final GrowthService growthService;
    private final AppUserRepo appUserRepo;
    private final UserCardRepo userCardRepo;
    private final UserCourseRepo userCourseRepo;
    private final FeedbackRepo feedbackRepo;
    private final StorageService storageService;

    @PostMapping("/auth/login")
    public ApiResponse<?> login(@RequestBody(required = false) Map<String, String> body,
                               jakarta.servlet.http.HttpServletRequest request) {
        String code = body == null ? null : body.get("code");
        String openid = header(request, "X-WX-OPENID", "x-wx-openid", "X-WX-FROM-OPENID", "x-wx-from-openid");
        if (openid == null) {
            openid = findHeader(request, "openid");
        }
        String unionid = header(request, "X-WX-UNIONID", "x-wx-unionid");
        return ApiResponse.ok(appAuthService.login(code, openid, unionid));
    }

    private static String header(jakarta.servlet.http.HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String findHeader(jakarta.servlet.http.HttpServletRequest request, String keyword) {
        var names = request.getHeaderNames();
        if (names == null) {
            return null;
        }
        String key = keyword.toLowerCase();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name != null && name.toLowerCase().contains(key)) {
                String value = request.getHeader(name);
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    @GetMapping("/auth/profile")
    public ApiResponse<?> profile() {
        return ApiResponse.ok(appAuthService.me(AuthContext.requireApp().id()));
    }

    @PostMapping("/auth/profile")
    public ApiResponse<?> saveProfile(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(appAuthService.completeProfile(AuthContext.requireApp().id(), body));
    }

    @PostMapping("/upload")
    public ApiResponse<?> upload(@RequestBody Map<String, String> body) {
        AuthContext.requireApp();
        String raw = body == null ? null : body.get("fileBase64");
        if (raw == null || raw.isBlank()) {
            raw = body == null ? null : body.get("imageBase64");
        }
        if (raw == null || raw.isBlank()) {
            throw new com.forget.academy.common.BizException("请选择文件");
        }
        int comma = raw.indexOf(',');
        if (comma >= 0) {
            raw = raw.substring(comma + 1);
        }
        byte[] bytes;
        try {
            bytes = java.util.Base64.getDecoder().decode(raw);
        } catch (IllegalArgumentException e) {
            throw new com.forget.academy.common.BizException("文件格式不正确");
        }
        String filename = body.get("filename");
        return ApiResponse.ok(storageService.saveFileBytes(bytes, filename));
    }

    @PostMapping("/upload-media")
    public ApiResponse<?> uploadMedia(@RequestParam("file") MultipartFile file) {
        AuthContext.requireApp();
        return ApiResponse.ok(storageService.saveMedia(file));
    }

    @GetMapping("/mine")
    public ApiResponse<?> mine(@RequestParam(required = false) String campusId) {
        Long userId = AuthContext.requireApp().id();
        AppUser user = appUserRepo.findById(userId).orElseThrow();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user", AppAuthService.toUserMap(user));
        data.put("growth", growthService.overview(user, campusId));
        data.put("cards", userCardRepo.findByUserIdOrderByIdDesc(userId));
        data.put("courses", userCourseRepo.findByUserIdOrderByIdDesc(userId));
        data.put("bookings", bookingService.myBookings(userId));
        data.put("waitlist", bookingService.myWaitlist(userId));
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

    @GetMapping("/waitlist")
    public ApiResponse<?> waitlist() {
        return ApiResponse.ok(bookingService.myWaitlist(AuthContext.requireApp().id()));
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
        return ApiResponse.ok(checkinPendingService.submitScan(AuthContext.requireApp().id(), body.get("payload")));
    }

    @GetMapping("/growth")
    public ApiResponse<?> growth(@RequestParam(required = false) String campusId) {
        AppUser user = appUserRepo.findById(AuthContext.requireApp().id()).orElseThrow();
        return ApiResponse.ok(growthService.overview(user, campusId));
    }

    @GetMapping("/opportunities")
    public ApiResponse<?> opportunities(@RequestParam String trackKey,
                                        @RequestParam(required = false) String campusId) {
        Long userId = AuthContext.get() != null && AuthContext.ROLE_APP.equals(AuthContext.get().role())
                ? AuthContext.get().id() : null;
        return ApiResponse.ok(growthService.listByTrack(trackKey, userId, campusId));
    }

    @PostMapping("/opportunities/{id}/apply")
    public ApiResponse<?> apply(@org.springframework.web.bind.annotation.PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        String resumeUrl = body == null ? null : body.get("resumeUrl");
        String resumeName = body == null ? null : body.get("resumeName");
        return ApiResponse.ok(growthService.toggleApply(AuthContext.requireApp().id(), id, resumeUrl, resumeName));
    }

    @PostMapping("/feedback")
    public ApiResponse<?> feedback(@RequestBody(required = false) Map<String, String> body) {
        Long userId = AuthContext.requireApp().id();
        String raw = body == null ? null : body.get("content");
        String content = raw == null ? "" : raw.trim();
        if (content.length() < 5) {
            throw new com.forget.academy.common.BizException("请至少填写 5 个字");
        }
        if (content.length() > 500) {
            throw new com.forget.academy.common.BizException("反馈内容过长");
        }
        String contact = body == null || body.get("contact") == null ? "" : body.get("contact").trim();
        if (contact.isBlank()) {
            throw new com.forget.academy.common.BizException("请填写联系方式");
        }
        if (contact.length() > 40) {
            throw new com.forget.academy.common.BizException("联系方式过长");
        }
        AppUser user = appUserRepo.findById(userId).orElseThrow();
        Feedback item = new Feedback();
        item.setUserId(userId);
        item.setNickname(user.getNickname());
        item.setCampusId(body == null ? null : body.get("campusId"));
        item.setContact(contact);
        item.setContent(content);
        feedbackRepo.save(item);
        return ApiResponse.ok(Map.of("ok", true, "message", "已提交"));
    }
}
