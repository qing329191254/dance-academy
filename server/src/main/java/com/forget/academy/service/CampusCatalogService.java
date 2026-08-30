package com.forget.academy.service;

import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Banner;
import com.forget.academy.entity.BrandPhoto;
import com.forget.academy.entity.CheckinPending;
import com.forget.academy.entity.CheckinSession;
import com.forget.academy.entity.ClassArchive;
import com.forget.academy.entity.EmployeeDutyRecord;
import com.forget.academy.entity.EmployeeProfile;
import com.forget.academy.entity.Feedback;
import com.forget.academy.entity.GrowthTrack;
import com.forget.academy.entity.PracticeRecord;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.School;
import com.forget.academy.entity.Studio;
import com.forget.academy.entity.Survey;
import com.forget.academy.entity.SurveyResponse;
import com.forget.academy.entity.TeacherAttendance;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BrandPhotoRepo;
import com.forget.academy.repo.CheckinPendingRepo;
import com.forget.academy.repo.CheckinSessionRepo;
import com.forget.academy.repo.ClassArchiveRepo;
import com.forget.academy.repo.EmployeeDutyRecordRepo;
import com.forget.academy.repo.EmployeeProfileRepo;
import com.forget.academy.repo.FeedbackRepo;
import com.forget.academy.repo.GrowthTrackRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.SchoolRepo;
import com.forget.academy.repo.StudioRepo;
import com.forget.academy.repo.SurveyRepo;
import com.forget.academy.repo.SurveyResponseRepo;
import com.forget.academy.repo.TeacherAttendanceRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 小程序左上角切换 / 后台校区筛选，与「校区管理员」配置同源。
 * campusId 使用 school.id 的字符串形式。
 */
@Service
@RequiredArgsConstructor
public class CampusCatalogService {
    private static final Logger log = LoggerFactory.getLogger(CampusCatalogService.class);

    private static final Map<String, String> LEGACY_KEY_TO_SCHOOL = Map.of(
            "shizishan", "四川师范大学",
            "chenglong", "四川师范大学",
            "bnu-zhuhai", "北京师范大学（珠海）",
            "uic", "北师香港浸会大学",
            "cdu", "成都大学",
            "swpu", "西南石油大学"
    );

    private final SchoolRepo schoolRepo;
    private final StudioRepo studioRepo;
    private final ScheduleRepo scheduleRepo;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;
    private final GrowthTrackRepo growthTrackRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final FeedbackRepo feedbackRepo;
    private final SurveyRepo surveyRepo;
    private final SurveyResponseRepo surveyResponseRepo;
    private final ClassArchiveRepo classArchiveRepo;
    private final CheckinSessionRepo checkinSessionRepo;
    private final CheckinPendingRepo checkinPendingRepo;
    private final TeacherAttendanceRepo teacherAttendanceRepo;
    private final EmployeeDutyRecordRepo employeeDutyRecordRepo;
    private final EmployeeProfileRepo employeeProfileRepo;
    private final AppUserRepo appUserRepo;
    private final AdminUserRepo adminUserRepo;

    public List<School> listEnabled() {
        return schoolRepo.findByEnabledTrueOrderBySortOrderAscIdAsc();
    }

    public List<School> listAll() {
        return schoolRepo.findAllByOrderBySortOrderAscIdAsc();
    }

    public List<String> allKeys() {
        List<School> list = listAll();
        if (list.isEmpty()) {
            return CampusIds.ALL;
        }
        return list.stream().map(this::keyOf).toList();
    }

    public boolean contains(String campusKey) {
        if (campusKey == null || campusKey.isBlank()) {
            return false;
        }
        String key = campusKey.trim();
        if (findSchoolByKey(key).isPresent()) {
            return true;
        }
        return CampusIds.ALL.contains(key) || LEGACY_KEY_TO_SCHOOL.containsKey(key);
    }

    public String normalize(String campusId) {
        if (campusId != null && !campusId.isBlank()) {
            String key = campusId.trim();
            Optional<School> school = findSchoolByKey(key);
            if (school.isPresent()) {
                return keyOf(school.get());
            }
            String mapped = resolveLegacyToSchoolKey(key);
            if (mapped != null) {
                return mapped;
            }
        }
        return defaultKey();
    }

    public String defaultKey() {
        return listEnabled().stream().findFirst().map(this::keyOf)
                .orElseGet(() -> listAll().stream().findFirst().map(this::keyOf).orElse(CampusIds.DEFAULT));
    }

    public String displayName(String campusKey) {
        if (campusKey == null || campusKey.isBlank()) {
            return "-";
        }
        return findSchoolByKey(campusKey.trim())
                .map(School::getName)
                .orElseGet(() -> {
                    String legacy = LEGACY_KEY_TO_SCHOOL.get(campusKey.trim());
                    return legacy != null ? legacy : campusKey;
                });
    }

    public String shortName(String campusKey) {
        String name = displayName(campusKey);
        if (name == null || "-".equals(name)) {
            return name;
        }
        if (name.length() <= 6) {
            return name;
        }
        String shortName = name
                .replace("（珠海）", "")
                .replace("(珠海)", "")
                .replace("师范大学", "师大")
                .replace("大学", "");
        return shortName.length() > 8 ? name.substring(0, 6) : shortName;
    }

    public List<Map<String, Object>> toAppList() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (School school : listEnabled()) {
            result.add(toSwitchItem(school));
        }
        return result;
    }

    public Map<String, Object> toSwitchItem(School school) {
        Map<String, Object> map = new LinkedHashMap<>();
        String key = keyOf(school);
        map.put("id", key);
        map.put("campusKey", key);
        map.put("name", school.getName());
        map.put("shortName", shortName(key));
        map.put("sortOrder", school.getSortOrder());
        map.put("enabled", Boolean.TRUE.equals(school.getEnabled()));
        return map;
    }

    @Transactional
    public void migrateLegacyCampusIds() {
        Map<String, String> legacyToTarget = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : LEGACY_KEY_TO_SCHOOL.entrySet()) {
            schoolRepo.findByName(entry.getValue()).ifPresent(school -> {
                String target = keyOf(school);
                if (!entry.getKey().equals(target)) {
                    legacyToTarget.put(entry.getKey(), target);
                }
            });
        }
        if (legacyToTarget.isEmpty()) {
            return;
        }
        log.info("Migrating legacy campus ids: {}", legacyToTarget);
        for (Map.Entry<String, String> entry : legacyToTarget.entrySet()) {
            reassignCampusId(entry.getKey(), entry.getValue());
        }
        migrateAdminCampusIds(legacyToTarget);
    }

    private void reassignCampusId(String from, String to) {
        studioRepo.findByCampusId(from).ifPresent(studio -> {
            if (studioRepo.findByCampusId(to).isPresent()) {
                studioRepo.delete(studio);
            } else {
                studio.setCampusId(to);
                studioRepo.save(studio);
            }
        });
        reassignAll(scheduleRepo.findAll(), from, to, Schedule::getCampusId, Schedule::setCampusId, scheduleRepo::save);
        reassignAll(bannerRepo.findAll(), from, to, Banner::getCampusId, Banner::setCampusId, bannerRepo::save);
        reassignAll(brandPhotoRepo.findAll(), from, to, BrandPhoto::getCampusId, BrandPhoto::setCampusId, brandPhotoRepo::save);
        reassignAll(growthTrackRepo.findAll(), from, to, GrowthTrack::getCampusId, GrowthTrack::setCampusId, growthTrackRepo::save);
        reassignAll(practiceRecordRepo.findAll(), from, to, PracticeRecord::getCampusId, PracticeRecord::setCampusId, practiceRecordRepo::save);
        reassignAll(feedbackRepo.findAll(), from, to, Feedback::getCampusId, Feedback::setCampusId, feedbackRepo::save);
        reassignAll(surveyRepo.findAll(), from, to, Survey::getCampusId, Survey::setCampusId, surveyRepo::save);
        reassignAll(surveyResponseRepo.findAll(), from, to, SurveyResponse::getCampusId, SurveyResponse::setCampusId, surveyResponseRepo::save);
        reassignAll(classArchiveRepo.findAll(), from, to, ClassArchive::getCampusId, ClassArchive::setCampusId, classArchiveRepo::save);
        reassignAll(checkinSessionRepo.findAll(), from, to, CheckinSession::getCampusId, CheckinSession::setCampusId, checkinSessionRepo::save);
        reassignAll(checkinPendingRepo.findAll(), from, to, CheckinPending::getCampusId, CheckinPending::setCampusId, checkinPendingRepo::save);
        reassignAll(teacherAttendanceRepo.findAll(), from, to, TeacherAttendance::getCampusId, TeacherAttendance::setCampusId, teacherAttendanceRepo::save);
        reassignAll(employeeDutyRecordRepo.findAll(), from, to, EmployeeDutyRecord::getCampusId, EmployeeDutyRecord::setCampusId, employeeDutyRecordRepo::save);
        reassignAll(employeeProfileRepo.findAll(), from, to, EmployeeProfile::getCampusId, EmployeeProfile::setCampusId, employeeProfileRepo::save);
        for (AppUser user : appUserRepo.findAll()) {
            if (from.equals(user.getCampusId())) {
                user.setCampusId(to);
                appUserRepo.save(user);
            }
        }
    }

    private void migrateAdminCampusIds(Map<String, String> legacyToTarget) {
        for (AdminUser admin : adminUserRepo.findAll()) {
            if (admin.getCampusIds() == null || admin.getCampusIds().isBlank()) {
                continue;
            }
            Set<String> next = new LinkedHashSet<>();
            boolean changed = false;
            for (String part : admin.getCampusIds().split(",")) {
                String key = part.trim();
                if (key.isBlank()) {
                    continue;
                }
                if (legacyToTarget.containsKey(key)) {
                    next.add(legacyToTarget.get(key));
                    changed = true;
                } else {
                    next.add(key);
                }
            }
            if (changed) {
                admin.setCampusIds(String.join(",", next));
                adminUserRepo.save(admin);
            }
        }
    }

    private interface Getter<T> { String get(T item); }
    private interface Setter<T> { void set(T item, String value); }
    private interface Saver<T> { void save(T item); }

    private <T> void reassignAll(List<T> items, String from, String to, Getter<T> getter, Setter<T> setter, Saver<T> saver) {
        for (T item : items) {
            if (from.equals(getter.get(item))) {
                setter.set(item, to);
                saver.save(item);
            }
        }
    }

    private String resolveLegacyToSchoolKey(String legacyKey) {
        String schoolName = LEGACY_KEY_TO_SCHOOL.get(legacyKey);
        if (schoolName == null) {
            return null;
        }
        return schoolRepo.findByName(schoolName).map(this::keyOf).orElse(null);
    }

    private Optional<School> findSchoolByKey(String key) {
        try {
            Long id = Long.valueOf(key);
            return schoolRepo.findById(id);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String keyOf(School school) {
        return String.valueOf(school.getId());
    }

    @Transactional
    public void ensureSeeded() {
        // 校区列表来自 school_option（校区管理员）
    }
}
