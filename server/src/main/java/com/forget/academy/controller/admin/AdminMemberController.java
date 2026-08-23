package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.UserCard;
import com.forget.academy.entity.UserCourse;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.UserCardRepo;
import com.forget.academy.repo.UserCourseRepo;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMemberController {
    private final AppUserRepo appUserRepo;
    private final UserCardRepo userCardRepo;
    private final UserCourseRepo userCourseRepo;

    @GetMapping("/users")
    public ApiResponse<?> users(@RequestParam(defaultValue = "") String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.ok(PageResult.of(appUserRepo.findAll(pageable)));
        }
        return ApiResponse.ok(PageResult.of(
                appUserRepo.findByNicknameContainingOrOpenidContaining(keyword, keyword, pageable)));
    }

    @GetMapping("/users/{id}")
    public ApiResponse<AppUser> user(@PathVariable Long id) {
        return ApiResponse.ok(appUserRepo.findById(id).orElseThrow(() -> new BizException("学员不存在")));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<AppUser> updateUser(@PathVariable Long id, @RequestBody AppUser body) {
        AppUser user = appUserRepo.findById(id).orElseThrow(() -> new BizException("学员不存在"));
        if (body.getNickname() != null) {
            user.setNickname(body.getNickname());
        }
        if (body.getAvatar() != null) {
            user.setAvatar(body.getAvatar());
        }
        if (body.getGender() != null) {
            user.setGender(body.getGender());
        }
        if (body.getBirthday() != null) {
            user.setBirthday(body.getBirthday());
        }
        if (body.getWorkLevel() != null) {
            user.setWorkLevel(body.getWorkLevel());
        }
        if (body.getWorkStage() != null) {
            user.setWorkStage(body.getWorkStage());
        }
        if (body.getDanceLevel() != null) {
            user.setDanceLevel(body.getDanceLevel());
        }
        if (body.getDanceStage() != null) {
            user.setDanceStage(body.getDanceStage());
        }
        if (body.getProfileComplete() != null) {
            user.setProfileComplete(body.getProfileComplete());
        }
        return ApiResponse.ok(appUserRepo.save(user));
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
