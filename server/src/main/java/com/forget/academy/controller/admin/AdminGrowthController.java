package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.entity.GrowthTrack;
import com.forget.academy.repo.GrowthTrackRepo;
import com.forget.academy.service.GrowthContentService;
import com.forget.academy.service.StudioService;
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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminGrowthController {
    private final GrowthTrackRepo growthTrackRepo;
    private final GrowthContentService growthContentService;
    private final StudioService studioService;

    @GetMapping("/growth-tracks")
    public ApiResponse<?> tracks(@RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        return ApiResponse.ok(growthTrackRepo.findByCampusIdOrderByLineKeyAscSortOrderAscIdAsc(resolved).stream()
                .map(growthContentService::toTrackMap)
                .toList());
    }

    @PostMapping("/growth-tracks")
    public ApiResponse<GrowthTrack> create(@RequestBody GrowthTrack body,
                                           @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        body.setId(null);
        body.setCampusId(resolved);
        normalize(body);
        return ApiResponse.ok(growthTrackRepo.save(body));
    }

    @PutMapping("/growth-tracks/{id}")
    public ApiResponse<GrowthTrack> update(@PathVariable Long id,
                                           @RequestBody GrowthTrack body,
                                           @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        GrowthTrack track = growthTrackRepo.findById(id).orElseThrow(() -> new BizException("赛道不存在"));
        if (track.getCampusId() != null && !track.getCampusId().equals(resolved)) {
            throw new BizException("赛道不属于当前校区");
        }
        track.setTrackKey(body.getTrackKey());
        track.setLineKey(body.getLineKey());
        track.setLineName(body.getLineName());
        track.setName(body.getName());
        track.setLevel(body.getLevel());
        track.setDescription(body.getDescription());
        track.setSortOrder(body.getSortOrder());
        track.setEnabled(body.getEnabled());
        track.setCampusId(resolved);
        normalize(track);
        return ApiResponse.ok(growthTrackRepo.save(track));
    }

    @DeleteMapping("/growth-tracks/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        GrowthTrack track = growthTrackRepo.findById(id).orElseThrow(() -> new BizException("赛道不存在"));
        if (track.getCampusId() != null && !track.getCampusId().equals(resolved)) {
            throw new BizException("赛道不属于当前校区");
        }
        growthTrackRepo.deleteById(id);
        return ApiResponse.ok();
    }

    private void normalize(GrowthTrack track) {
        if (track.getTrackKey() == null || track.getTrackKey().isBlank()) {
            throw new BizException("请填写赛道标识");
        }
        track.setTrackKey(track.getTrackKey().trim());
        if (track.getLineKey() == null || track.getLineKey().isBlank()) {
            throw new BizException("请填写成长线");
        }
        track.setLineKey(track.getLineKey().trim());
        if (track.getCampusId() == null || track.getCampusId().isBlank()) {
            throw new BizException("请选择校区");
        }
        if (track.getEnabled() == null) {
            track.setEnabled(true);
        }
        if (track.getSortOrder() == null) {
            track.setSortOrder(0);
        }
        growthTrackRepo.findByCampusIdAndTrackKey(track.getCampusId(), track.getTrackKey()).ifPresent(existing -> {
            if (track.getId() == null || !existing.getId().equals(track.getId())) {
                throw new BizException("该校区下赛道标识已存在");
            }
        });
    }
}
