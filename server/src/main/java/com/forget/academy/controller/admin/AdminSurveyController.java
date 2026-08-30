package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.service.SurveyService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/admin/surveys")
@RequiredArgsConstructor
public class AdminSurveyController {
    private final SurveyService surveyService;

    @GetMapping
    public ApiResponse<?> list(@RequestParam String campusId) {
        return ApiResponse.ok(surveyService.adminList(campusId));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.ok(surveyService.adminDetail(id));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestParam String campusId, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(surveyService.saveSurvey(campusId, body));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id,
                                 @RequestParam String campusId,
                                 @RequestBody Map<String, Object> body) {
        body.put("id", id);
        return ApiResponse.ok(surveyService.saveSurvey(campusId, body));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        surveyService.deleteSurvey(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/responses")
    public ApiResponse<?> responses(@PathVariable Long id,
                                    @RequestParam(defaultValue = "") String keyword,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(surveyService.adminResponses(id, keyword, page, size));
    }
}
