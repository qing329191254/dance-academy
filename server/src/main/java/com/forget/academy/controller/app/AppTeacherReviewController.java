package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.TeacherReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppTeacherReviewController {
    private final TeacherReviewService teacherReviewService;

    @PostMapping("/teacher-reviews")
    public ApiResponse<?> submit(@RequestBody Map<String, Object> body) {
        Long teacherId = parseLong(body == null ? null : body.get("teacherId"));
        String content = body == null || body.get("content") == null ? "" : String.valueOf(body.get("content"));
        if (teacherId == null) {
            throw new com.forget.academy.common.BizException("请选择老师");
        }
        teacherReviewService.submit(AuthContext.requireApp().id(), teacherId, content);
        return ApiResponse.ok(Map.of("message", "已提交，感谢反馈"));
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
}
