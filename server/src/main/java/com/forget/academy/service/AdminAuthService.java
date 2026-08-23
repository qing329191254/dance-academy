package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminUserRepo adminUserRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(String username, String password) {
        AdminUser admin = adminUserRepo.findByUsername(username)
                .orElseThrow(() -> new BizException(401, "账号或密码错误"));
        if (!encoder.matches(password, admin.getPasswordHash())) {
            throw new BizException(401, "账号或密码错误");
        }
        String token = jwtUtil.create(admin.getId(), AuthContext.ROLE_ADMIN, admin.getName());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("name", admin.getName());
        return data;
    }

    public Map<String, Object> me(Long id) {
        AdminUser admin = adminUserRepo.findById(id).orElseThrow(() -> new BizException("管理员不存在"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("name", admin.getName());
        data.put("role", admin.getRole());
        return data;
    }
}
