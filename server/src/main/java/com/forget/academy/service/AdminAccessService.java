package com.forget.academy.service;

import com.forget.academy.common.AdminRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.entity.AppUser;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.UserCampusRepo;
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
    private final CampusCatalogService campusCatalogService;
    private final UserCampusRepo userCampusRepo;
    private final ScheduleRepo scheduleRepo;

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
            return campusCatalogService.allKeys();
        }
        List<String> campuses = parseCampusIds(admin);
        if (campuses.isEmpty()) {
            throw new BizException("管理员账号未分配校区，请联系超级管理员");
        }
        return campuses.stream()
                .filter(campusCatalogService::contains)
                .map(campusCatalogService::normalize)
                .distinct()
                .toList();
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

    /**
     * 校区管理员只能管理：本校区员工、在本校区有课的教师、已关联本校区的学员。
     * 超级管理员不受限。认领接口请单独放行，不要走本方法。
     */
    public void assertCanManageUser(AppUser user) {
        if (user == null || user.getId() == null) {
            throw new BizException("用户不存在");
        }
        AdminUser admin = currentAdmin();
        if (isSuperAdmin(admin)) {
            return;
        }
        List<String> allowed = allowedCampusIds(admin);
        String role = user.getRole() == null ? "student" : user.getRole().trim().toLowerCase();
        if ("employee".equals(role)) {
            if (user.getCampusId() != null && allowed.contains(user.getCampusId().trim())) {
                return;
            }
            throw new BizException(403, "无权管理该员工");
        }
        if ("teacher".equals(role)) {
            if (user.getTeacherId() != null && scheduleRepo.existsByTeacherIdAndCampusIdIn(user.getTeacherId(), allowed)) {
                return;
            }
            throw new BizException(403, "无权管理该教师账号");
        }
        if (userCampusRepo.existsByUserIdAndCampusIdIn(user.getId(), allowed)) {
            return;
        }
        throw new BizException(403, "无权管理该学员，请先将其加入本校区");
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

    public List<String> normalizeCampusIds(List<String> campusIds) {
        if (campusIds == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String campusId : campusIds) {
            if (campusId != null && !campusId.isBlank() && campusCatalogService.contains(campusId.trim())) {
                result.add(campusId.trim());
            }
        }
        return result.stream().distinct().toList();
    }
}
