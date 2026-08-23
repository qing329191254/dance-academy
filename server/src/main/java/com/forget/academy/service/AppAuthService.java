package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.AppUser;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.security.AuthContext;
import com.forget.academy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppAuthService {
    private final AppUserRepo appUserRepo;
    private final WxAuthService wxAuthService;
    private final JwtUtil jwtUtil;

    @Transactional
    public Map<String, Object> login(String code) {
        WxAuthService.WxSession wx = wxAuthService.resolveOpenid(code);
        AppUser user = appUserRepo.findByOpenid(wx.openid()).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setOpenid(wx.openid());
            created.setUnionid(wx.unionid());
            created.setNickname("");
            created.setAvatar("");
            created.setProfileComplete(false);
            created.setWorkLevel("T1");
            created.setWorkStage("兼职");
            created.setDanceLevel("T1");
            created.setDanceStage("演出");
            return appUserRepo.save(created);
        });
        if (wx.unionid() != null && (user.getUnionid() == null || user.getUnionid().isBlank())) {
            user.setUnionid(wx.unionid());
            appUserRepo.save(user);
        }
        String token = jwtUtil.create(user.getId(), AuthContext.ROLE_APP, user.getNickname());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("user", toUserMap(user));
        return data;
    }

    @Transactional
    public Map<String, Object> completeProfile(Long userId, Map<String, String> body) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        String nickname = safe(body.get("nickname"));
        if (nickname.isBlank()) {
            nickname = "学员";
        }
        user.setNickname(nickname);
        user.setAvatar(firstNonBlank(body.get("avatar"), user.getAvatar(), "/static/avatars/guest.png"));
        user.setGender(firstNonBlank(body.get("gender"), user.getGender(), "女"));
        if (body.containsKey("birthday")) {
            user.setBirthday(body.get("birthday"));
        }
        user.setProfileComplete(true);
        appUserRepo.save(user);
        return toUserMap(user);
    }

    public Map<String, Object> me(Long userId) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        return toUserMap(user);
    }

    public static Map<String, Object> toUserMap(AppUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("gender", user.getGender());
        map.put("birthday", user.getBirthday());
        map.put("profileComplete", Boolean.TRUE.equals(user.getProfileComplete()));
        map.put("workLevel", user.getWorkLevel());
        map.put("workStage", user.getWorkStage());
        map.put("danceLevel", user.getDanceLevel());
        map.put("danceStage", user.getDanceStage());
        return map;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
