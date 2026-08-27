package com.forget.academy.service;

import com.forget.academy.entity.GrowthTrack;
import com.forget.academy.entity.Studio;
import com.forget.academy.repo.GrowthTrackRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrowthContentService {
    private final StudioService studioService;
    private final CampusContentService campusContentService;
    private final GrowthTrackRepo growthTrackRepo;

    public Map<String, Object> content(String campusId) {
        String resolved = campusContentService.normalizeCampusId(campusId);
        Studio studio = studioService.resolveForApp(resolved);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("intro", defaultText(studio.getGrowthIntro(),
                "FOR-GET不仅是上课，我们把兼职、实习、就业演出、商演、考证等多样化资源嫁接给大家，用舞蹈发展、勤工俭学两条成长线，帮你从学员走向舞台与职场，愿你的大学，因为有FG而更好。"));
        data.put("levelTip", defaultText(studio.getGrowthLevelTip(),
                "新学员默认享有 T1 权益，可通过年限、考核等途径升级至 T2 / T3。"));
        data.put("workLead", defaultText(studio.getWorkLead(),
                "勤工俭学成长线：从校园兼职到实习，再到管理角色。点击进入可查看近期机会并报名。"));
        data.put("danceLead", defaultText(studio.getDanceLead(),
                "舞蹈发展成长线：演出练胆 → 商演实践 → 教师考证与任教。点击进入可查看近期机会并报名。"));
        data.put("workModuleSummary", defaultText(studio.getWorkModuleSummary(),
                "兼职 → 实习 → 管理（T1-T3）"));
        data.put("danceModuleSummary", defaultText(studio.getDanceModuleSummary(),
                "演出 → 商演 → 教师（T1-T3）"));
        data.put("workTracks", tracks(resolved, "work"));
        data.put("danceTracks", tracks(resolved, "dance"));
        data.put("trackMeta", trackMetaMap(resolved));
        return data;
    }

    public Map<String, Map<String, String>> trackMetaMap(String campusId) {
        String resolved = campusContentService.normalizeCampusId(campusId);
        Map<String, Map<String, String>> map = new LinkedHashMap<>();
        for (GrowthTrack track : growthTrackRepo.findByCampusIdOrderByLineKeyAscSortOrderAscIdAsc(resolved)) {
            if (!Boolean.TRUE.equals(track.getEnabled())) {
                continue;
            }
            map.put(track.getTrackKey(), Map.of(
                    "line", track.getLineName() == null ? "" : track.getLineName(),
                    "name", track.getName() == null ? "" : track.getName(),
                    "level", track.getLevel() == null ? "" : track.getLevel()));
        }
        return map;
    }

    public String pathForLine(String campusId, String lineKey) {
        String resolved = campusContentService.normalizeCampusId(campusId);
        List<GrowthTrack> tracks = growthTrackRepo
                .findByCampusIdAndLineKeyAndEnabledTrueOrderBySortOrderAscIdAsc(resolved, lineKey);
        if (tracks.isEmpty()) {
            return "work".equals(lineKey) ? "兼职 → 实习 → 管理" : "演出 → 商演 → 教师";
        }
        return tracks.stream().map(GrowthTrack::getName).collect(Collectors.joining(" → "));
    }

    private List<Map<String, Object>> tracks(String campusId, String lineKey) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (GrowthTrack track : growthTrackRepo
                .findByCampusIdAndLineKeyAndEnabledTrueOrderBySortOrderAscIdAsc(campusId, lineKey)) {
            list.add(toTrackMap(track));
        }
        return list;
    }

    public Map<String, Object> toTrackMap(GrowthTrack track) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", track.getId());
        map.put("key", track.getTrackKey());
        map.put("trackKey", track.getTrackKey());
        map.put("lineKey", track.getLineKey());
        map.put("line", track.getLineName());
        map.put("name", track.getName());
        map.put("level", track.getLevel());
        map.put("desc", track.getDescription());
        map.put("description", track.getDescription());
        map.put("sortOrder", track.getSortOrder());
        map.put("enabled", track.getEnabled());
        return map;
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
