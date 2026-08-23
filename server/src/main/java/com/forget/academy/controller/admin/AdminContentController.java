package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.entity.Banner;
import com.forget.academy.entity.BrandPhoto;
import com.forget.academy.entity.Studio;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BrandPhotoRepo;
import com.forget.academy.repo.StudioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminContentController {
    private final StudioRepo studioRepo;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;

    @GetMapping("/studio")
    public ApiResponse<Studio> getStudio() {
        return ApiResponse.ok(studioRepo.findAll().stream().findFirst().orElseGet(Studio::new));
    }

    @PutMapping("/studio")
    public ApiResponse<Studio> saveStudio(@RequestBody Studio body) {
        Studio studio = studioRepo.findAll().stream().findFirst().orElseGet(Studio::new);
        studio.setName(body.getName());
        studio.setLocation(body.getLocation());
        studio.setCity(body.getCity());
        studio.setAddress(body.getAddress());
        studio.setLatitude(body.getLatitude());
        studio.setLongitude(body.getLongitude());
        studio.setBusinessHours(body.getBusinessHours());
        studio.setPhone(body.getPhone());
        studio.setPhoneDisplay(body.getPhoneDisplay());
        studio.setLogo(body.getLogo());
        studio.setSplashImage(body.getSplashImage());
        studio.setIntro(body.getIntro());
        studio.setBusiness(body.getBusiness());
        studio.setSlogan(body.getSlogan());
        return ApiResponse.ok(studioRepo.save(studio));
    }

    @GetMapping("/banners")
    public ApiResponse<?> banners() {
        return ApiResponse.ok(bannerRepo.findAllByOrderBySortOrderAscIdAsc());
    }

    @PostMapping("/banners")
    public ApiResponse<Banner> createBanner(@RequestBody Banner body) {
        body.setId(null);
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
        return ApiResponse.ok(bannerRepo.save(body));
    }

    @PutMapping("/banners/{id}")
    public ApiResponse<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner body) {
        Banner banner = bannerRepo.findById(id).orElseThrow();
        banner.setImageUrl(body.getImageUrl());
        banner.setSortOrder(body.getSortOrder());
        banner.setEnabled(body.getEnabled());
        return ApiResponse.ok(bannerRepo.save(banner));
    }

    @DeleteMapping("/banners/{id}")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        bannerRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/brand-photos")
    public ApiResponse<?> photos() {
        return ApiResponse.ok(brandPhotoRepo.findAllByOrderBySortOrderAscIdAsc());
    }

    @PostMapping("/brand-photos")
    public ApiResponse<BrandPhoto> createPhoto(@RequestBody BrandPhoto body) {
        body.setId(null);
        if (body.getSortOrder() == null) {
            body.setSortOrder(0);
        }
        return ApiResponse.ok(brandPhotoRepo.save(body));
    }

    @PutMapping("/brand-photos/{id}")
    public ApiResponse<BrandPhoto> updatePhoto(@PathVariable Long id, @RequestBody BrandPhoto body) {
        BrandPhoto photo = brandPhotoRepo.findById(id).orElseThrow();
        photo.setImageUrl(body.getImageUrl());
        photo.setSortOrder(body.getSortOrder());
        return ApiResponse.ok(brandPhotoRepo.save(photo));
    }

    @DeleteMapping("/brand-photos/{id}")
    public ApiResponse<Void> deletePhoto(@PathVariable Long id) {
        brandPhotoRepo.deleteById(id);
        return ApiResponse.ok();
    }
}
