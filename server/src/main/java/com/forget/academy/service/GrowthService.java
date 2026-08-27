package com.forget.academy.service;

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
    private final GrowthContentService growthContentService;

    public Map<String, Object> overview(AppUser user, String campusId) {
        Map<String, Object> work = new LinkedHashMap<>();
        work.put("line", "勤工俭学");
        work.put("current", user.getWorkStage());
        work.put("level", user.getWorkLevel());
        work.put("path", growthContentService.pathForLine(campusId, "work"));
        work.put("url", "/pages/growth/work");

        Map<String, Object> dance = new LinkedHashMap<>();
        dance.put("line", "舞蹈发展");
        dance.put("current", user.getDanceStage());
        dance.put("level", user.getDanceLevel());
        dance.put("path", growthContentService.pathForLine(campusId, "dance"));
        dance.put("url", "/pages/growth/dance");

        return Map.of("work", work, "dance", dance);
    }

    public List<Map<String, Object>> listByTrack(String trackKey, Long userId, String campusId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Opportunity item : opportunityRepo.findByTrackKeyAndEnabledTrueOrderByIdAsc(trackKey)) {
            Map<String, Object> row = toOppMap(item, campusId);
            if (userId != null) {
                applyRepo.findByUserIdAndOpportunityId(userId, item.getId()).ifPresent(apply -> {
                    boolean active = !"cancelled".equals(apply.getStatus());
                    row.put("applied", active);
                    if (active) {
                        row.put("resumeUrl", apply.getResumeUrl());
                        row.put("resumeName", apply.getResumeName());
                    }
                });
            }
            list.add(row);
        }
        return list;
    }

    @Transactional
    public Map<String, Object> toggleApply(Long userId, Long opportunityId, String resumeUrl, String resumeName) {
        Opportunity opportunity = opportunityRepo.findById(opportunityId)
                .orElseThrow(() -> new com.forget.academy.common.BizException("机会不存在"));
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new com.forget.academy.common.BizException("用户不存在"));
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
            fillResume(apply, resumeUrl, resumeName);
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
        fillResume(apply, resumeUrl, resumeName);
        try {
            applyRepo.save(apply);
        } catch (DataIntegrityViolationException e) {
            throw new com.forget.academy.common.BizException("请勿重复报名");
        }
        return Map.of("applied", true, "message", "报名已提交");
    }

    private static void fillResume(OpportunityApply apply, String resumeUrl, String resumeName) {
        if (resumeUrl == null || resumeUrl.isBlank()) {
            return;
        }
        apply.setResumeUrl(resumeUrl.trim());
        apply.setResumeName(resumeName == null || resumeName.isBlank() ? "个人简历" : resumeName.trim());
    }

    public Map<String, Object> toOppMap(Opportunity item, String campusId) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("code", item.getCode());
        map.put("trackKey", item.getTrackKey());
        map.put("title", item.getTitle());
        map.put("deadline", item.getDeadline());
        map.put("spots", item.getSpots());
        map.put("level", item.getLevel());
        map.put("summary", item.getSummary());
        map.put("meta", growthContentService.trackMetaMap(campusId)
                .getOrDefault(item.getTrackKey(), Map.of("name", "成长")));
        return map;
    }
}
