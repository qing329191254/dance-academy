package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.entity.Banner;
import com.forget.academy.entity.BrandPhoto;
import com.forget.academy.entity.Studio;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BrandPhotoRepo;
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
public class AdminContentController {
    private final StudioService studioService;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;

    @GetMapping("/studio")
    public ApiResponse<Studio> getStudio(@RequestParam String campusId) {
        return ApiResponse.ok(studioService.getForAdmin(campusId));
    }

    @PutMapping("/studio")
    public ApiResponse<Studio> saveStudio(@RequestBody Studio body,
                                          @RequestParam String campusId) {
        return ApiResponse.ok(studioService.saveForAdmin(campusId, body));
    }

    @GetMapping("/banners")
    public ApiResponse<?> banners(@RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        return ApiResponse.ok(bannerRepo.findByCampusIdOrderBySortOrderAscIdAsc(resolved));
    }

    @PostMapping("/banners")
    public ApiResponse<Banner> createBanner(@RequestBody Banner body,
                                            @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        body.setId(null);
        body.setCampusId(resolved);
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
        return ApiResponse.ok(bannerRepo.save(body));
    }

    @PutMapping("/banners/{id}")
    public ApiResponse<Banner> updateBanner(@PathVariable Long id,
                                            @RequestBody Banner body,
                                            @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        Banner banner = requireBanner(id, resolved);
        banner.setImageUrl(body.getImageUrl());
        banner.setSortOrder(body.getSortOrder());
        banner.setEnabled(body.getEnabled());
        return ApiResponse.ok(bannerRepo.save(banner));
    }

    @DeleteMapping("/banners/{id}")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id,
                                          @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        requireBanner(id, resolved);
        bannerRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/brand-photos")
    public ApiResponse<?> photos(@RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        return ApiResponse.ok(brandPhotoRepo.findByCampusIdOrderBySortOrderAscIdAsc(resolved));
    }

    @PostMapping("/brand-photos")
    public ApiResponse<BrandPhoto> createPhoto(@RequestBody BrandPhoto body,
                                               @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        body.setId(null);
        body.setCampusId(resolved);
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
        return ApiResponse.ok(brandPhotoRepo.save(body));
    }

    @PutMapping("/brand-photos/{id}")
    public ApiResponse<BrandPhoto> updatePhoto(@PathVariable Long id,
                                               @RequestBody BrandPhoto body,
                                               @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        BrandPhoto photo = requirePhoto(id, resolved);
        photo.setImageUrl(body.getImageUrl());
        photo.setSortOrder(body.getSortOrder());
        return ApiResponse.ok(brandPhotoRepo.save(photo));
    }

    @DeleteMapping("/brand-photos/{id}")
    public ApiResponse<Void> deletePhoto(@PathVariable Long id,
                                         @RequestParam String campusId) {
        String resolved = studioService.requireAdminCampusId(campusId);
        requirePhoto(id, resolved);
        brandPhotoRepo.deleteById(id);
        return ApiResponse.ok();
    }

    private Banner requireBanner(Long id, String campusId) {
        Banner banner = bannerRepo.findById(id).orElseThrow(() -> new BizException("轮播不存在"));
        if (banner.getCampusId() != null && !banner.getCampusId().equals(campusId)) {
            throw new BizException("轮播不属于当前校区");
        }
        return banner;
    }

    private BrandPhoto requirePhoto(Long id, String campusId) {
        BrandPhoto photo = brandPhotoRepo.findById(id).orElseThrow(() -> new BizException("照片不存在"));
        if (photo.getCampusId() != null && !photo.getCampusId().equals(campusId)) {
            throw new BizException("照片不属于当前校区");
        }
        return photo;
    }
}
