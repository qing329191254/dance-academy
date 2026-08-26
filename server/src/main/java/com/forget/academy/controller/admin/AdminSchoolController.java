package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.entity.School;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.SchoolRepo;
import com.forget.academy.service.AdminAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminSchoolController {
    private final SchoolRepo schoolRepo;
    private final AppUserRepo appUserRepo;
    private final AdminAccessService adminAccessService;

    @GetMapping("/schools")
    public ApiResponse<?> schools(@RequestParam(defaultValue = "false") boolean all) {
        if (all) {
            adminAccessService.requireSuperAdmin();
            return ApiResponse.ok(schoolRepo.findAllByOrderBySortOrderAscIdAsc());
        }
        return ApiResponse.ok(schoolRepo.findByEnabledTrueOrderBySortOrderAscIdAsc());
    }

    @PostMapping("/schools")
    public ApiResponse<School> create(@RequestBody School body) {
        adminAccessService.requireSuperAdmin();
        String name = normalizeName(body.getName());
        if (schoolRepo.existsByName(name)) {
            throw new BizException("学校名称已存在");
        }
        School school = new School();
        school.setName(name);
        school.setSortOrder(body.getSortOrder() == null ? 0 : body.getSortOrder());
        school.setEnabled(body.getEnabled() == null || body.getEnabled());
        return ApiResponse.ok(schoolRepo.save(school));
    }

    @PutMapping("/schools/{id}")
    public ApiResponse<School> update(@PathVariable Long id, @RequestBody School body) {
        adminAccessService.requireSuperAdmin();
        School school = schoolRepo.findById(id).orElseThrow(() -> new BizException("学校不存在"));
        String name = normalizeName(body.getName());
        schoolRepo.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BizException("学校名称已存在");
            }
        });
        school.setName(name);
        if (body.getSortOrder() != null) {
            school.setSortOrder(body.getSortOrder());
        }
        if (body.getEnabled() != null) {
            school.setEnabled(body.getEnabled());
        }
        return ApiResponse.ok(schoolRepo.save(school));
    }

    @DeleteMapping("/schools/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminAccessService.requireSuperAdmin();
        School school = schoolRepo.findById(id).orElseThrow(() -> new BizException("学校不存在"));
        long used = appUserRepo.countBySchool(school.getName());
        if (used > 0) {
            throw new BizException("已有 " + used + " 名学员使用该学校，无法删除，可改为停用");
        }
        schoolRepo.delete(school);
        return ApiResponse.ok();
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException("请填写学校名称");
        }
        return name.trim();
    }
}
