package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppSurveyController {
    private final SurveyService surveyService;

    @GetMapping("/surveys")
    public ApiResponse<?> list(@RequestParam(required = false) String campusId) {
        return ApiResponse.ok(surveyService.appList(campusId, AuthContext.requireApp().id()));
    }

    @GetMapping("/surveys/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.ok(surveyService.appDetail(id, AuthContext.requireApp().id()));
    }

    @PostMapping("/surveys/{id}/submit")
    public ApiResponse<?> submit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        body.put("surveyId", id);
        return ApiResponse.ok(surveyService.submit(AuthContext.requireApp().id(), body));
    }
}
