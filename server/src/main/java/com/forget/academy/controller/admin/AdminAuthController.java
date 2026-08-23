package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(adminAuthService.login(body.get("username"), body.get("password")));
    }

    @GetMapping("/me")
    public ApiResponse<?> me() {
        return ApiResponse.ok(adminAuthService.me(AuthContext.requireAdmin().id()));
    }
}
