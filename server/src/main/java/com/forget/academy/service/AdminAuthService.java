package com.forget.academy.service;

import com.forget.academy.common.AdminRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminUserRepo adminUserRepo;
    private final AdminAccessService adminAccessService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(String username, String password) {
        AdminUser admin = adminUserRepo.findByUsername(username)
                .orElseThrow(() -> new BizException(401, "账号或密码错误"));
        if (!encoder.matches(password, admin.getPasswordHash())) {
            throw new BizException(401, "账号或密码错误");
        }
        String token = jwtUtil.create(admin.getId(), AuthContext.ROLE_ADMIN, admin.getName());
        Map<String, Object> data = adminAccessService.toProfile(admin);
        data.put("token", token);
        return data;
    }

    public Map<String, Object> me(Long id) {
        AdminUser admin = adminUserRepo.findById(id).orElseThrow(() -> new BizException("管理员不存在"));
        return adminAccessService.toProfile(admin);
    }

    public List<Map<String, Object>> listAdmins() {
        adminAccessService.requireSuperAdmin();
        return adminUserRepo.findAll().stream()
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .map(adminAccessService::toProfile)
                .toList();
    }

    @Transactional
    public Map<String, Object> createAdmin(Map<String, Object> body) {
        adminAccessService.requireSuperAdmin();
        String username = str(body.get("username"));
        String password = str(body.get("password"));
        String name = str(body.get("name"));
        if (username.isBlank() || password.isBlank()) {
            throw new BizException("请填写账号和密码");
        }
        if (adminUserRepo.findByUsername(username).isPresent()) {
            throw new BizException("账号已存在");
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash(encoder.encode(password));
        admin.setName(name.isBlank() ? username : name);
        if (isSuperAdminRequest(body)) {
            applySuperAdmin(admin);
        } else {
            applyPrincipalCampuses(admin, body.get("campusIds"));
        }
        return adminAccessService.toProfile(adminUserRepo.save(admin));
    }

    @Transactional
    public Map<String, Object> updateAdmin(Long id, Map<String, Object> body) {
        adminAccessService.requireSuperAdmin();
        AdminUser admin = adminUserRepo.findById(id).orElseThrow(() -> new BizException("管理员不存在"));
        String name = str(body.get("name"));
        if (!name.isBlank()) {
            admin.setName(name);
        }
        String password = str(body.get("password"));
        if (!password.isBlank()) {
            admin.setPasswordHash(encoder.encode(password));
        }
        boolean currentlySuper = adminAccessService.isSuperAdmin(admin);
        if (currentlySuper) {
            // 已有超管不可降级
            if (body.containsKey("superAdmin") && !isSuperAdminRequest(body)) {
                throw new BizException("不能将超级管理员降级为普通管理员");
            }
            if (body.get("role") != null) {
                String role = str(body.get("role"));
                if (!role.isBlank() && !AdminRoles.isSuperAdmin(role)) {
                    throw new BizException("不能修改超级管理员的角色");
                }
            }
            applySuperAdmin(admin);
        } else if (isSuperAdminRequest(body)) {
            applySuperAdmin(admin);
        } else if (body.get("campusIds") != null || body.containsKey("superAdmin")) {
            applyPrincipalCampuses(admin, body.get("campusIds"));
        }
        return adminAccessService.toProfile(adminUserRepo.save(admin));
    }

    @Transactional
    public void deleteAdmin(Long id) {
        adminAccessService.requireSuperAdmin();
        AdminUser current = adminAccessService.currentAdmin();
        if (current.getId().equals(id)) {
            throw new BizException("不能删除当前登录账号");
        }
        adminUserRepo.deleteById(id);
    }

    private void applySuperAdmin(AdminUser admin) {
        admin.setRole(AdminRoles.SUPER_ADMIN);
        admin.setCampusIds("");
    }

    private void applyPrincipalCampuses(AdminUser admin, Object campusIdsRaw) {
        admin.setRole(AdminRoles.PRINCIPAL);
        List<String> campuses = adminAccessService.normalizeCampusIds(parseCampusIds(campusIdsRaw));
        if (campuses.isEmpty()) {
            throw new BizException("请为管理员分配至少一个校区");
        }
        admin.setCampusIds(AdminAccessService.joinCampusIds(campuses));
    }

    private boolean isSuperAdminRequest(Map<String, Object> body) {
        if (body == null) {
            return false;
        }
        Object flag = body.get("superAdmin");
        if (flag instanceof Boolean bool) {
            return bool;
        }
        if (flag != null) {
            String text = String.valueOf(flag).trim();
            if ("true".equalsIgnoreCase(text) || "1".equals(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text) || "0".equals(text)) {
                return false;
            }
        }
        String role = str(body.get("role"));
        return AdminRoles.SUPER_ADMIN.equals(role);
    }

    @SuppressWarnings("unchecked")
    private List<String> parseCampusIds(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        String text = String.valueOf(raw).trim();
        if (text.isBlank()) {
            return List.of();
        }
        return List.of(text.split(","));
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
