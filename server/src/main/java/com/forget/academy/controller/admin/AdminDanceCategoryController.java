package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.entity.DanceCategory;
import com.forget.academy.service.DanceCategoryService;
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
public class AdminDanceCategoryController {
    private final DanceCategoryService danceCategoryService;

    @GetMapping("/dance-categories")
    public ApiResponse<?> tree(@RequestParam(defaultValue = "true") boolean all) {
        return ApiResponse.ok(danceCategoryService.tree(all));
    }

    @GetMapping("/dance-sections")
    public ApiResponse<?> sections(@RequestParam(defaultValue = "true") boolean enabledOnly) {
        return ApiResponse.ok(danceCategoryService.listSections(enabledOnly));
    }

    @PostMapping("/dance-categories")
    public ApiResponse<DanceCategory> create(@RequestBody DanceCategory body) {
        return ApiResponse.ok(danceCategoryService.create(body));
    }

    @PutMapping("/dance-categories/{id}")
    public ApiResponse<DanceCategory> update(@PathVariable Long id, @RequestBody DanceCategory body) {
        return ApiResponse.ok(danceCategoryService.update(id, body));
    }

    @DeleteMapping("/dance-categories/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        danceCategoryService.delete(id);
        return ApiResponse.ok();
    }
}
