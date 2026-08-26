package com.forget.academy.service;

import com.forget.academy.common.AppRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Teacher;
import com.forget.academy.entity.TeacherReview;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.repo.TeacherReviewRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherReviewService {
    private final TeacherReviewRepo teacherReviewRepo;
    private final TeacherRepo teacherRepo;
    private final AppUserRepo appUserRepo;
    private final TeacherService teacherService;

    @Transactional
    public TeacherReview submit(Long userId, Long teacherId, String content) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        String role = user.getRole() == null ? AppRoles.STUDENT : user.getRole().trim().toLowerCase();
        if (!AppRoles.STUDENT.equals(role)) {
            throw new BizException("仅学员可提交评价");
        }
        Teacher teacher = teacherRepo.findById(teacherId).orElseThrow(() -> new BizException("老师不存在"));
        if (!Boolean.TRUE.equals(teacher.getEnabled())) {
            throw new BizException("该老师暂不可评价");
        }
        String text = content == null ? "" : content.trim();
        if (text.length() < 5) {
            throw new BizException("请至少填写 5 个字");
        }
        if (text.length() > 2000) {
            throw new BizException("评价内容过长");
        }
        TeacherReview review = new TeacherReview();
        review.setTeacherId(teacherId);
        review.setUserId(userId);
        review.setNickname(firstNonBlank(user.getNickname(), "学员"));
        review.setContent(text);
        return teacherReviewRepo.save(review);
    }

    public PageResult<Map<String, Object>> listForTeacher(Long userId, int page, int size) {
        AppUser user = teacherService.requireTeacherUser(userId);
        return toPageResult(teacherReviewRepo.findByTeacherIdOrderByIdDesc(
                user.getTeacherId(), pageable(page, size)));
    }

    @Transactional
    public void deleteByTeacher(Long userId, Long reviewId) {
        AppUser user = teacherService.requireTeacherUser(userId);
        TeacherReview review = teacherReviewRepo.findById(reviewId)
                .orElseThrow(() -> new BizException("评价不存在"));
        if (!user.getTeacherId().equals(review.getTeacherId())) {
            throw new BizException("无权删除该评价");
        }
        teacherReviewRepo.delete(review);
    }

    public PageResult<Map<String, Object>> listForAdmin(Long teacherId, String keyword, int page, int size) {
        String query = keyword == null ? "" : keyword.trim();
        return toPageResult(teacherReviewRepo.search(teacherId, query, pageable(page, size)));
    }

    @Transactional
    public void deleteByAdmin(Long reviewId) {
        if (!teacherReviewRepo.existsById(reviewId)) {
            throw new BizException("评价不存在");
        }
        teacherReviewRepo.deleteById(reviewId);
    }

    private PageResult<Map<String, Object>> toPageResult(org.springframework.data.domain.Page<TeacherReview> pageResult) {
        Map<Long, String> teacherNames = new LinkedHashMap<>();
        for (TeacherReview review : pageResult.getContent()) {
            teacherNames.putIfAbsent(review.getTeacherId(), resolveTeacherName(review.getTeacherId()));
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (TeacherReview review : pageResult.getContent()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", review.getId());
            row.put("teacherId", review.getTeacherId());
            row.put("teacherName", teacherNames.get(review.getTeacherId()));
            row.put("userId", review.getUserId());
            row.put("nickname", review.getNickname());
            row.put("content", review.getContent());
            row.put("createdAt", review.getCreatedAt() == null ? null : review.getCreatedAt().toEpochMilli());
            list.add(row);
        }
        return new PageResult<>(list, pageResult.getTotalElements(), pageResult.getNumber() + 1, pageResult.getSize());
    }

    private String resolveTeacherName(Long teacherId) {
        if (teacherId == null) {
            return "-";
        }
        return teacherRepo.findById(teacherId).map(Teacher::getName).orElse("-");
    }

    private static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(size, 1), 50));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
