package com.forget.academy.service;

import com.forget.academy.common.AdminRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.security.AdminContext;
import com.forget.academy.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAccessService {
    private final AdminUserRepo adminUserRepo;

    public AdminUser currentAdmin() {
        AdminUser cached = AdminContext.get();
        if (cached != null) {
            return cached;
        }
        AuthContext.AuthUser auth = AuthContext.requireAdmin();
        AdminUser admin = adminUserRepo.findById(auth.id())
                .orElseThrow(() -> new BizException(401, "管理员不存在"));
        AdminContext.set(admin);
        return admin;
    }

    public boolean isSuperAdmin(AdminUser admin) {
        return AdminRoles.isSuperAdmin(admin.getRole());
    }

    public List<String> parseCampusIds(AdminUser admin) {
        if (admin.getCampusIds() == null || admin.getCampusIds().isBlank()) {
            return List.of();
        }
        return Arrays.stream(admin.getCampusIds().split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }

    public List<String> allowedCampusIds(AdminUser admin) {
        if (isSuperAdmin(admin)) {
            return CampusIds.ALL;
        }
        List<String> campuses = parseCampusIds(admin);
        if (campuses.isEmpty()) {
            throw new BizException("校长账号未分配校区，请联系超级管理员");
        }
        return campuses.stream().filter(CampusIds.ALL::contains).toList();
    }

    public List<String> resolveCampusScope(String requestedCampusId) {
        AdminUser admin = currentAdmin();
        List<String> allowed = allowedCampusIds(admin);
        if (requestedCampusId != null && !requestedCampusId.isBlank()) {
            String campus = requestedCampusId.trim();
            if (!allowed.contains(campus)) {
                throw new BizException(403, "无权访问该校区");
            }
            return List.of(campus);
        }
        return allowed;
    }

    public void assertCanAccessCampus(String campusId) {
        if (campusId == null || campusId.isBlank()) {
            return;
        }
        resolveCampusScope(campusId);
    }

    public void requireSuperAdmin() {
        if (!isSuperAdmin(currentAdmin())) {
            throw new BizException(403, "仅超级管理员可执行此操作");
        }
    }

    public Map<String, Object> toProfile(AdminUser admin) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", admin.getId());
        map.put("username", admin.getUsername());
        map.put("name", admin.getName());
        map.put("role", normalizeRole(admin.getRole()));
        map.put("campusIds", allowedCampusIds(admin));
        map.put("superAdmin", isSuperAdmin(admin));
        return map;
    }

    public static String normalizeRole(String role) {
        return AdminRoles.isSuperAdmin(role) ? AdminRoles.SUPER_ADMIN : AdminRoles.PRINCIPAL;
    }

    public static String joinCampusIds(List<String> campusIds) {
        if (campusIds == null || campusIds.isEmpty()) {
            return null;
        }
        return campusIds.stream().map(String::trim).filter(s -> !s.isBlank()).distinct()
                .collect(Collectors.joining(","));
    }

    public static List<String> normalizeCampusIds(List<String> campusIds) {
        if (campusIds == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String campusId : campusIds) {
            if (campusId != null && !campusId.isBlank() && CampusIds.ALL.contains(campusId.trim())) {
                result.add(campusId.trim());
            }
        }
        return result.stream().distinct().toList();
    }
}
