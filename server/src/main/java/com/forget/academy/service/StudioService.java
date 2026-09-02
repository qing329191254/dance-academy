package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.Studio;
import com.forget.academy.repo.StudioRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudioService {
    private final StudioRepo studioRepo;
    private final AdminAccessService adminAccessService;
    private final CampusCatalogService campusCatalogService;

    /** 成长文案等随校区配置，读当前校区记录 */
    public Studio resolveForApp(String campusId) {
        String normalized = normalizeCampusId(campusId);
        return studioRepo.findByCampusId(normalized)
                .or(() -> studioRepo.findByCampusId(CampusIds.DEFAULT))
                .or(() -> studioRepo.findAll().stream().findFirst())
                .orElseGet(Studio::new);
    }

    /** @deprecated 成长文案已按校区配置，请使用 resolveForApp */
    public Studio resolveOrgDefault() {
        return studioRepo.findByCampusId(CampusIds.DEFAULT)
                .or(() -> studioRepo.findAll().stream().findFirst())
                .orElseGet(Studio::new);
    }

    public Studio getForAdmin(String campusId) {
        String resolved = requireAdminCampusId(campusId);
        return studioRepo.findByCampusId(resolved).orElseGet(() -> newTemplate(resolved));
    }

    @Transactional
    public Studio saveForAdmin(String campusId, Studio body) {
        String resolved = requireAdminCampusId(campusId);
        Studio studio = studioRepo.findByCampusId(resolved).orElseGet(() -> newTemplate(resolved));
        applyBody(studio, body);
        studio.setCampusId(resolved);
        return studioRepo.save(studio);
    }

    public String requireAdminCampusId(String campusId) {
        if (campusId == null || campusId.isBlank()) {
            throw new BizException("请选择校区");
        }
        adminAccessService.resolveCampusScope(campusId.trim());
        return campusId.trim();
    }

    @Transactional
    public void ensureCampusRecords() {
        Studio template = studioRepo.findAll().stream()
                .filter(item -> item.getCampusId() == null || item.getCampusId().isBlank())
                .findFirst()
                .orElseGet(() -> studioRepo.findByCampusId(CampusIds.DEFAULT).orElse(null));

        if (template != null && (template.getCampusId() == null || template.getCampusId().isBlank())) {
            template.setCampusId(CampusIds.DEFAULT);
            studioRepo.save(template);
        }
        if (template == null) {
            template = studioRepo.findByCampusId(CampusIds.DEFAULT).orElse(null);
        }

        for (String campusId : campusCatalogService.allKeys()) {
            if (studioRepo.findByCampusId(campusId).isPresent()) {
                continue;
            }
            Studio studio = template == null ? newTemplate(campusId) : copyFrom(template, campusId);
            studioRepo.save(studio);
        }
    }

    private Studio newTemplate(String campusId) {
        Studio studio = new Studio();
        studio.setCampusId(campusId);
        studio.setName("高校FOR-GET舞室");
        studio.setLogo("/logo.png");
        studio.setBusinessHours("营业时间 13:00-22:00");
        studio.setPhone("02888881234");
        studio.setPhoneDisplay("028-8888-1234");
        studio.setCity("四川成都");
        studio.setAddress("四川省成都市");
        studio.setLatitude(30.659462);
        studio.setLongitude(104.065735);
        studio.setIntro("深耕高校街舞文化的俱乐部品牌。课堂之外，用勤工俭学与舞蹈发展双线赋能大学生成长，增强机构黏性。");
        studio.setBusiness("团课 / 固定班 / 私教课 / 成长中心");
        studio.setSlogan("DANCE UP · BREAK FREE");
        studio.setShareTitle("高校FOR-GET舞室");
        studio.setCourseSystemLead("按学习方式和目标选择：固定班、次通卡、私教，或定制赛事与商演。");
        studio.setCourseSystemHomeSummary("特色固定班、次卡、通卡\n私教、定制课、商演赛事");
        applyCampusDefaults(studio, campusId);
        return studio;
    }

    private Studio copyFrom(Studio template, String campusId) {
        Studio studio = new Studio();
        applyBody(studio, template);
        studio.setId(null);
        studio.setCampusId(campusId);
        applyCampusDefaults(studio, campusId);
        return studio;
    }

    private void applyCampusDefaults(Studio studio, String campusId) {
        String label = campusLabel(campusId);
        studio.setLocation("四川成都 · " + label);
        if (studio.getName() == null || studio.getName().isBlank() || "高校FOR-GET舞室".equals(studio.getName())) {
            studio.setName("高校FOR-GET舞室 · " + shortCampusLabel(campusId));
        }
    }

    private void applyBody(Studio target, Studio body) {
        target.setName(body.getName());
        target.setLocation(body.getLocation());
        target.setCity(body.getCity());
        target.setAddress(body.getAddress());
        target.setLatitude(body.getLatitude());
        target.setLongitude(body.getLongitude());
        target.setBusinessHours(body.getBusinessHours());
        target.setPhone(body.getPhone());
        target.setPhoneDisplay(body.getPhoneDisplay());
        target.setLogo(body.getLogo());
        target.setSplashImage(body.getSplashImage());
        if (body.getShareTitle() != null) {
            target.setShareTitle(body.getShareTitle());
        }
        if (body.getShareImage() != null) {
            target.setShareImage(body.getShareImage());
        }
        target.setIntro(body.getIntro());
        target.setBusiness(body.getBusiness());
        target.setSlogan(body.getSlogan());
        target.setCourseSystemLead(body.getCourseSystemLead());
        target.setCourseSystemHomeSummary(body.getCourseSystemHomeSummary());
        target.setGrowthIntro(body.getGrowthIntro());
        target.setGrowthLevelTip(body.getGrowthLevelTip());
        target.setWorkLead(body.getWorkLead());
        target.setDanceLead(body.getDanceLead());
        target.setWorkModuleSummary(body.getWorkModuleSummary());
        target.setDanceModuleSummary(body.getDanceModuleSummary());
        target.setStudentNotice(body.getStudentNotice());
    }

    private String normalizeCampusId(String campusId) {
        return campusCatalogService.normalize(campusId);
    }

    private String campusLabel(String campusId) {
        return campusCatalogService.displayName(campusId);
    }

    private String shortCampusLabel(String campusId) {
        return campusCatalogService.shortName(campusId);
    }
}
