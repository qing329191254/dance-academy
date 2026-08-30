package com.forget.academy.service;

import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.Banner;
import com.forget.academy.entity.BrandPhoto;
import com.forget.academy.entity.GrowthTrack;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BrandPhotoRepo;
import com.forget.academy.repo.GrowthTrackRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusContentService {
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;
    private final GrowthTrackRepo growthTrackRepo;
    private final CampusCatalogService campusCatalogService;

    public String normalizeCampusId(String campusId) {
        return campusCatalogService.normalize(campusId);
    }

    @Transactional
    public void ensureCampusRecords() {
        assignDefaultCampusToBanners();
        assignDefaultCampusToPhotos();
        assignDefaultCampusToTracks();
        ensureTracksForAllCampuses();
        ensureBannersForAllCampuses();
        ensurePhotosForAllCampuses();
    }

    private void assignDefaultCampusToBanners() {
        for (Banner item : bannerRepo.findAll()) {
            if (item.getCampusId() == null || item.getCampusId().isBlank()) {
                item.setCampusId(CampusIds.DEFAULT);
                bannerRepo.save(item);
            }
        }
    }

    private void assignDefaultCampusToPhotos() {
        for (BrandPhoto item : brandPhotoRepo.findAll()) {
            if (item.getCampusId() == null || item.getCampusId().isBlank()) {
                item.setCampusId(CampusIds.DEFAULT);
                brandPhotoRepo.save(item);
            }
        }
    }

    private void assignDefaultCampusToTracks() {
        for (GrowthTrack item : growthTrackRepo.findAll()) {
            if (item.getCampusId() == null || item.getCampusId().isBlank()) {
                item.setCampusId(CampusIds.DEFAULT);
                growthTrackRepo.save(item);
            }
        }
    }

    private void ensureTracksForAllCampuses() {
        List<GrowthTrack> template = growthTrackRepo
                .findByCampusIdOrderByLineKeyAscSortOrderAscIdAsc(CampusIds.DEFAULT);
        if (template.isEmpty()) {
            return;
        }
        for (String campusId : campusCatalogService.allKeys()) {
            if (growthTrackRepo.countByCampusId(campusId) > 0) {
                continue;
            }
            for (GrowthTrack track : template) {
                growthTrackRepo.save(copyTrack(track, campusId));
            }
        }
    }

    private void ensureBannersForAllCampuses() {
        List<Banner> template = bannerRepo.findByCampusIdOrderBySortOrderAscIdAsc(CampusIds.DEFAULT);
        if (template.isEmpty()) {
            return;
        }
        for (String campusId : campusCatalogService.allKeys()) {
            if (bannerRepo.countByCampusId(campusId) > 0) {
                continue;
            }
            for (Banner banner : template) {
                bannerRepo.save(copyBanner(banner, campusId));
            }
        }
    }

    private void ensurePhotosForAllCampuses() {
        List<BrandPhoto> template = brandPhotoRepo.findByCampusIdOrderBySortOrderAscIdAsc(CampusIds.DEFAULT);
        if (template.isEmpty()) {
            return;
        }
        for (String campusId : campusCatalogService.allKeys()) {
            if (brandPhotoRepo.countByCampusId(campusId) > 0) {
                continue;
            }
            for (BrandPhoto photo : template) {
                brandPhotoRepo.save(copyPhoto(photo, campusId));
            }
        }
    }

    private static GrowthTrack copyTrack(GrowthTrack source, String campusId) {
        GrowthTrack track = new GrowthTrack();
        track.setCampusId(campusId);
        track.setTrackKey(source.getTrackKey());
        track.setLineKey(source.getLineKey());
        track.setLineName(source.getLineName());
        track.setName(source.getName());
        track.setLevel(source.getLevel());
        track.setDescription(source.getDescription());
        track.setSortOrder(source.getSortOrder());
        track.setEnabled(source.getEnabled());
        return track;
    }

    private static Banner copyBanner(Banner source, String campusId) {
        Banner banner = new Banner();
        banner.setCampusId(campusId);
        banner.setImageUrl(source.getImageUrl());
        banner.setSortOrder(source.getSortOrder());
        banner.setEnabled(source.getEnabled());
        return banner;
    }

    private static BrandPhoto copyPhoto(BrandPhoto source, String campusId) {
        BrandPhoto photo = new BrandPhoto();
        photo.setCampusId(campusId);
        photo.setImageUrl(source.getImageUrl());
        photo.setSortOrder(source.getSortOrder());
        return photo;
    }
}
