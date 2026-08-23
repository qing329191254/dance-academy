package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Opportunity;
import com.forget.academy.entity.OpportunityApply;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.OpportunityApplyRepo;
import com.forget.academy.repo.OpportunityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrowthService {
    private final OpportunityRepo opportunityRepo;
    private final OpportunityApplyRepo applyRepo;
    private final AppUserRepo appUserRepo;

    public static final Map<String, Map<String, String>> TRACK_META = Map.of(
            "parttime", Map.of("line", "勤工俭学", "name", "兼职", "level", "T1"),
            "intern", Map.of("line", "勤工俭学", "name", "实习", "level", "T2"),
            "manage", Map.of("line", "勤工俭学", "name", "管理", "level", "T3"),
            "show", Map.of("line", "舞蹈发展", "name", "演出", "level", "T1"),
            "commercial", Map.of("line", "舞蹈发展", "name", "商演", "level", "T2"),
            "teacher", Map.of("line", "舞蹈发展", "name", "教师", "level", "T3")
    );

    public Map<String, Object> overview(AppUser user) {
        Map<String, Object> work = new LinkedHashMap<>();
        work.put("line", "勤工俭学");
        work.put("current", user.getWorkStage());
        work.put("level", user.getWorkLevel());
        work.put("path", "兼职 → 实习 → 管理");
        work.put("url", "/pages/growth/work");

        Map<String, Object> dance = new LinkedHashMap<>();
        dance.put("line", "舞蹈发展");
        dance.put("current", user.getDanceStage());
        dance.put("level", user.getDanceLevel());
        dance.put("path", "演出 → 商演 → 教师");
        dance.put("url", "/pages/growth/dance");

        return Map.of("work", work, "dance", dance);
    }

    public List<Map<String, Object>> listByTrack(String trackKey, Long userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Opportunity item : opportunityRepo.findByTrackKeyAndEnabledTrueOrderByIdAsc(trackKey)) {
            Map<String, Object> row = toOppMap(item);
            if (userId != null) {
                row.put("applied", applyRepo.findByUserIdAndOpportunityId(userId, item.getId())
                        .filter(a -> !"cancelled".equals(a.getStatus()))
                        .isPresent());
            }
            list.add(row);
        }
        return list;
    }

    @Transactional
    public Map<String, Object> toggleApply(Long userId, Long opportunityId) {
        Opportunity opportunity = opportunityRepo.findById(opportunityId)
                .orElseThrow(() -> new BizException("机会不存在"));
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        var existing = applyRepo.findByUserIdAndOpportunityId(userId, opportunityId);
        if (existing.isPresent() && !"cancelled".equals(existing.get().getStatus())) {
            OpportunityApply apply = existing.get();
            apply.setStatus("cancelled");
            applyRepo.save(apply);
            return Map.of("applied", false, "message", "已取消报名");
        }
        if (existing.isPresent()) {
            OpportunityApply apply = existing.get();
            apply.setStatus("pending");
            apply.setTitle(opportunity.getTitle());
            apply.setNickname(user.getNickname());
            applyRepo.save(apply);
            return Map.of("applied", true, "message", "报名已提交");
        }
        OpportunityApply apply = new OpportunityApply();
        apply.setUserId(userId);
        apply.setOpportunityId(opportunityId);
        apply.setTrackKey(opportunity.getTrackKey());
        apply.setTitle(opportunity.getTitle());
        apply.setNickname(user.getNickname());
        apply.setStatus("pending");
        try {
            applyRepo.save(apply);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("请勿重复报名");
        }
        return Map.of("applied", true, "message", "报名已提交");
    }

    public Map<String, Object> toOppMap(Opportunity item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("code", item.getCode());
        map.put("trackKey", item.getTrackKey());
        map.put("title", item.getTitle());
        map.put("deadline", item.getDeadline());
        map.put("spots", item.getSpots());
        map.put("level", item.getLevel());
        map.put("summary", item.getSummary());
        map.put("meta", TRACK_META.getOrDefault(item.getTrackKey(), Map.of("name", "成长")));
        return map;
    }
}
