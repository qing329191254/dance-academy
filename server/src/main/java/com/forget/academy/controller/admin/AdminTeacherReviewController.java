package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.PageResult;
import com.forget.academy.service.TeacherReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminTeacherReviewController {
    private final TeacherReviewService teacherReviewService;

    @GetMapping("/teacher-reviews")
    public ApiResponse<PageResult<Map<String, Object>>> list(@RequestParam(required = false) Long teacherId,
                                                             @RequestParam(defaultValue = "") String keyword,
                                                             @RequestParam(required = false) String campusId,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(teacherReviewService.listForAdmin(teacherId, keyword, campusId, page, size));
    }

    @DeleteMapping("/teacher-reviews/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teacherReviewService.deleteByAdmin(id);
        return ApiResponse.ok();
    }
}
