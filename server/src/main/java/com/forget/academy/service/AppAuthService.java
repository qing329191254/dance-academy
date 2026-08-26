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
        return login(code, null, null);
    }

    @Transactional
    public Map<String, Object> login(String code, String openid, String unionid) {
        WxAuthService.WxSession wx;
        if (openid != null && !openid.isBlank()) {
            wx = new WxAuthService.WxSession(openid, unionid == null || unionid.isBlank() ? null : unionid);
        } else {
            wx = wxAuthService.resolveOpenid(code);
        }
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
    public Map<String, Object> completeProfile(Long userId, Map<String, ?> body) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));

        String nickname = str(body == null ? null : body.get("nickname"));
        if (nickname.isBlank()) {
            throw new BizException("请输入真实姓名");
        }
        user.setNickname(nickname);

        String avatar = str(body == null ? null : body.get("avatar"));
        if (isUsableAvatar(avatar)) {
            user.setAvatar(avatar.length() > 2048 ? avatar.substring(0, 2048) : avatar);
        } else if (user.getAvatar() == null || user.getAvatar().isBlank()) {
            throw new BizException("请上传真实头像");
        }

        String gender = str(body == null ? null : body.get("gender"));
        if (gender.isBlank()) {
            throw new BizException("请选择性别");
        }
        user.setGender(gender);

        String birthday = str(body == null ? null : body.get("birthday"));
        if (birthday.isBlank()) {
            throw new BizException("请选择生日");
        }
        user.setBirthday(birthday);

        String phone = str(body == null ? null : body.get("phone"));
        if (phone.isBlank()) {
            throw new BizException("请输入电话");
        }
        if (!phone.matches("^1\\d{10}$")) {
            throw new BizException("请输入正确的手机号");
        }
        user.setPhone(phone);

        String school = str(body == null ? null : body.get("school"));
        if (school.isBlank()) {
            throw new BizException("请输入学校");
        }
        user.setSchool(school);

        String collegeGrade = str(body == null ? null : body.get("collegeGrade"));
        if (collegeGrade.isBlank()) {
            throw new BizException("请输入学院年级");
        }
        user.setCollegeGrade(collegeGrade);

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
        map.put("phone", user.getPhone());
        map.put("school", user.getSchool());
        map.put("collegeGrade", user.getCollegeGrade());
        map.put("role", user.getRole() == null ? "student" : user.getRole());
        map.put("teacherId", user.getTeacherId());
        map.put("campusId", user.getCampusId());
        map.put("profileComplete", Boolean.TRUE.equals(user.getProfileComplete()));
        map.put("workLevel", user.getWorkLevel());
        map.put("workStage", user.getWorkStage());
        map.put("danceLevel", user.getDanceLevel());
        map.put("danceStage", user.getDanceStage());
        return map;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean isUsableAvatar(String avatar) {
        if (avatar == null || avatar.isBlank()) {
            return false;
        }
        String value = avatar.toLowerCase();
        return !(value.startsWith("wxfile://")
                || value.startsWith("http://tmp/")
                || value.startsWith("https://tmp/")
                || value.startsWith("file://")
                || value.contains("://tmp/"));
    }
}
