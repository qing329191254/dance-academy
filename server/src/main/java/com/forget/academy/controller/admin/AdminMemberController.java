package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.OpportunityApply;
import com.forget.academy.entity.UserCard;
import com.forget.academy.entity.UserCourse;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.OpportunityApplyRepo;
import com.forget.academy.repo.UserCardRepo;
import com.forget.academy.repo.UserCourseRepo;
import com.forget.academy.service.AppAuthService;
import com.forget.academy.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMemberController {
    private final AppUserRepo appUserRepo;
    private final UserCardRepo userCardRepo;
    private final UserCourseRepo userCourseRepo;
    private final OpportunityApplyRepo opportunityApplyRepo;
    private final EmployeeService employeeService;

    @GetMapping("/users")
    public ApiResponse<PageResult<Map<String, Object>>> users(@RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        var pageResult = keyword == null || keyword.isBlank()
                ? appUserRepo.findAll(pageable)
                : appUserRepo.findByNicknameContainingOrOpenidContaining(keyword, keyword, pageable);
        List<Map<String, Object>> list = enrichUsers(pageResult.getContent());
        return ApiResponse.ok(new PageResult<>(list, pageResult.getTotalElements(), pageResult.getNumber() + 1, pageResult.getSize()));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AppUser> user(@PathVariable Long id) {
        return ApiResponse.ok(appUserRepo.findById(id).orElseThrow(() -> new BizException("学员不存在")));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<AppUser> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        AppUser user = appUserRepo.findById(id).orElseThrow(() -> new BizException("学员不存在"));
        if (body.get("nickname") != null) {
            user.setNickname(String.valueOf(body.get("nickname")));
        }
        if (body.get("avatar") != null) {
            user.setAvatar(String.valueOf(body.get("avatar")));
        }
        if (body.get("gender") != null) {
            user.setGender(String.valueOf(body.get("gender")));
        }
        if (body.get("birthday") != null) {
            user.setBirthday(String.valueOf(body.get("birthday")));
        }
        if (body.get("phone") != null) {
            user.setPhone(String.valueOf(body.get("phone")));
        }
        if (body.get("school") != null) {
            user.setSchool(String.valueOf(body.get("school")));
        }
        if (body.get("collegeGrade") != null) {
            user.setCollegeGrade(String.valueOf(body.get("collegeGrade")));
        }
        if (body.get("role") != null) {
            user.setRole(String.valueOf(body.get("role")));
        }
        if (body.get("teacherId") != null && !"".equals(String.valueOf(body.get("teacherId")))) {
            user.setTeacherId(Long.valueOf(String.valueOf(body.get("teacherId"))));
        } else if ("student".equals(user.getRole()) || "employee".equals(user.getRole())) {
            user.setTeacherId(null);
        }
        if (body.get("campusId") != null) {
            user.setCampusId(String.valueOf(body.get("campusId")).trim());
        }
        if ("teacher".equals(user.getRole())) {
            user.setCampusId(null);
        } else if ("student".equals(user.getRole())) {
            user.setTeacherId(null);
            user.setCampusId(null);
        } else if ("employee".equals(user.getRole())) {
            user.setTeacherId(null);
        }
        if (body.get("workLevel") != null) {
            user.setWorkLevel(String.valueOf(body.get("workLevel")));
        }
        if (body.get("workStage") != null) {
            user.setWorkStage(String.valueOf(body.get("workStage")));
        }
        if (body.get("danceLevel") != null) {
            user.setDanceLevel(String.valueOf(body.get("danceLevel")));
        }
        if (body.get("danceStage") != null) {
            user.setDanceStage(String.valueOf(body.get("danceStage")));
        }
        if (body.containsKey("closedClassGroup")) {
            String group = body.get("closedClassGroup") == null
                    ? null
                    : String.valueOf(body.get("closedClassGroup")).trim();
            if (group == null || group.isBlank()) {
                user.setClosedClassGroup(null);
            } else if (com.forget.academy.common.ClosedClassGroup.isValid(group)) {
                user.setClosedClassGroup(group);
            } else {
                throw new BizException("闭门课分组无效");
            }
        }
        if (body.get("profileComplete") != null) {
            user.setProfileComplete(Boolean.valueOf(String.valueOf(body.get("profileComplete"))));
        }
        AppUser saved = appUserRepo.save(user);
        if ("employee".equals(saved.getRole())) {
            employeeService.saveProfile(
                    saved.getId(),
                    saved.getCampusId(),
                    body.get("jobTitle") == null ? null : String.valueOf(body.get("jobTitle")),
                    body.get("jobDescription") == null ? null : String.valueOf(body.get("jobDescription")));
        }
        return ApiResponse.ok(saved);
    }

    @GetMapping("/cards")
    public ApiResponse<?> cards(@RequestParam(required = false) Long userId,
                                @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer size,
                                @RequestParam(defaultValue = "") String keyword,
                                @RequestParam(defaultValue = "") String type) {
        if (page == null) {
            List<UserCard> cards = userId != null
                    ? userCardRepo.findByUserIdOrderByIdDesc(userId)
                    : userCardRepo.findAll();
            fillCardUsers(cards);
            return ApiResponse.ok(cards);
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id"));
        String query = keyword == null ? "" : keyword.trim();
        String cardType = type == null ? "" : type.trim();
        var result = userCardRepo.search(query, userId, cardType, pageable);
        fillCardUsers(result.getContent());
        return ApiResponse.ok(PageResult.of(result));
    }

    @PostMapping("/cards")
    public ApiResponse<UserCard> createCard(@RequestBody UserCard body) {
        body.setUserId(resolveUserId(body));
        body.setId(null);
        return ApiResponse.ok(userCardRepo.save(body));
    }

    @PutMapping("/cards/{id}")
    public ApiResponse<UserCard> updateCard(@PathVariable Long id, @RequestBody UserCard body) {
        UserCard card = userCardRepo.findById(id).orElseThrow(() -> new BizException("卡包不存在"));
        card.setUserId(resolveUserId(body));
        card.setName(body.getName());
        card.setType(body.getType());
        card.setRemain(body.getRemain());
        card.setTotal(body.getTotal());
        card.setExpireDate(body.getExpireDate());
        card.setCover(body.getCover());
        return ApiResponse.ok(userCardRepo.save(card));
    }

    @DeleteMapping("/cards/{id}")
    public ApiResponse<Void> deleteCard(@PathVariable Long id) {
        userCardRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/user-courses")
    public ApiResponse<?> userCourses(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return ApiResponse.ok(userCourseRepo.findByUserIdOrderByIdDesc(userId));
        }
        return ApiResponse.ok(userCourseRepo.findAll());
    }

    @PostMapping("/user-courses")
    public ApiResponse<UserCourse> createUserCourse(@RequestBody UserCourse body) {
        body.setId(null);
        return ApiResponse.ok(userCourseRepo.save(body));
    }

    @PutMapping("/user-courses/{id}")
    public ApiResponse<UserCourse> updateUserCourse(@PathVariable Long id, @RequestBody UserCourse body) {
        UserCourse item = userCourseRepo.findById(id).orElseThrow(() -> new BizException("学员课程不存在"));
        item.setUserId(body.getUserId());
        item.setCourseId(body.getCourseId());
        item.setName(body.getName());
        item.setTeacherName(body.getTeacherName());
        item.setProgress(body.getProgress());
        item.setStatus(body.getStatus());
        return ApiResponse.ok(userCourseRepo.save(item));
    }

    @DeleteMapping("/user-courses/{id}")
    public ApiResponse<Void> deleteUserCourse(@PathVariable Long id) {
        userCourseRepo.deleteById(id);
        return ApiResponse.ok();
    }

    private Long resolveUserId(UserCard body) {
        if (body.getUserId() != null) {
            if (!appUserRepo.existsById(body.getUserId())) {
                throw new BizException("学员不存在");
            }
            return body.getUserId();
        }
        if (body.getOpenid() != null && !body.getOpenid().isBlank()) {
            return appUserRepo.findByOpenid(body.getOpenid().trim())
                    .map(AppUser::getId)
                    .orElseThrow(() -> new BizException("找不到该 OpenID 对应的学员，请到学员管理核对"));
        }
        throw new BizException("请选择学员");
    }

    private List<Map<String, Object>> enrichUsers(List<AppUser> users) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        var userIds = users.stream().map(AppUser::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> cardTypesByUser = new HashMap<>();
        for (UserCard card : userCardRepo.findByUserIdIn(userIds)) {
            if (card.getUserId() == null || card.getType() == null || card.getType().isBlank()) {
                continue;
            }
            cardTypesByUser.compute(card.getUserId(), (id, existing) -> {
                Set<String> types = new LinkedHashSet<>();
                if (existing != null && !existing.isBlank()) {
                    for (String part : existing.split("、")) {
                        if (!part.isBlank()) {
                            types.add(part.trim());
                        }
                    }
                }
                types.add(card.getType().trim());
                return String.join("、", types);
            });
        }
        Map<Long, OpportunityApply> resumeByUser = new HashMap<>();
        for (OpportunityApply apply : opportunityApplyRepo.findByUserIdInOrderByIdDesc(userIds)) {
            if (apply.getUserId() == null || resumeByUser.containsKey(apply.getUserId())) {
                continue;
            }
            if (apply.getResumeUrl() == null || apply.getResumeUrl().isBlank()) {
                continue;
            }
            resumeByUser.put(apply.getUserId(), apply);
        }
        List<Map<String, Object>> rows = new ArrayList<>(users.size());
        for (AppUser user : users) {
            Map<String, Object> row = AppAuthService.toUserMap(user);
            row.put("openid", user.getOpenid());
            row.put("cardTypes", cardTypesByUser.getOrDefault(user.getId(), ""));
            OpportunityApply apply = resumeByUser.get(user.getId());
            row.put("resumeUrl", apply == null ? null : apply.getResumeUrl());
            row.put("resumeName", apply == null ? null : apply.getResumeName());
            row.put("closedClassGroup", user.getClosedClassGroup());
            row.put("closedClassGroupLabel", com.forget.academy.common.ClosedClassGroup.label(user.getClosedClassGroup()));
            rows.add(row);
        }
        return rows;
    }

    private void fillCardUsers(List<UserCard> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        var ids = cards.stream().map(UserCard::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, AppUser> users = new HashMap<>();
        for (AppUser user : appUserRepo.findAllById(ids)) {
            users.put(user.getId(), user);
        }
        for (UserCard card : cards) {
            AppUser user = users.get(card.getUserId());
            if (user != null) {
                card.setNickname(user.getNickname());
                card.setOpenid(user.getOpenid());
            }
        }
    }
}
