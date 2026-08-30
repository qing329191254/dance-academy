package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.UserCampus;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.UserCampusRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCampusService {
    private final UserCampusRepo userCampusRepo;
    private final AdminAccessService adminAccessService;
    private final CampusCatalogService campusCatalogService;
    private final BookingRepo bookingRepo;
    private final PracticeRecordRepo practiceRecordRepo;

    public List<String> listCampusIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userCampusRepo.findByUserId(userId).stream()
                .map(UserCampus::getCampusId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
    }

    @Transactional
    public void ensureLinked(Long userId, String campusId) {
        if (userId == null) {
            return;
        }
        String campus = campusCatalogService.normalize(campusId);
        if (campus == null || campus.isBlank()) {
            return;
        }
        if (userCampusRepo.existsByUserIdAndCampusId(userId, campus)) {
            return;
        }
        UserCampus row = new UserCampus();
        row.setUserId(userId);
        row.setCampusId(campus);
        userCampusRepo.save(row);
    }

    /** 加入本校区（并存，不删除其他校区） */
    @Transactional
    public List<String> claimCampus(Long userId, String campusId) {
        if (userId == null) {
            throw new BizException("学员不存在");
        }
        String campus = campusId == null ? "" : campusId.trim();
        if (campus.isBlank()) {
            throw new BizException("请先在顶部选择校区");
        }
        adminAccessService.assertCanAccessCampus(campus);
        ensureLinked(userId, campus);
        return listCampusIds(userId);
    }

    /**
     * 保存关联校区。
     * 超管：以提交列表为准替换全部。
     * 管理员：只能增删自己权限内的校区，其他校区关联保持不变。
     */
    @Transactional
    public List<String> saveCampusLinks(Long userId, Collection<String> requested) {
        if (userId == null) {
            throw new BizException("学员不存在");
        }
        LinkedHashSet<String> wanted = new LinkedHashSet<>();
        if (requested != null) {
            for (String raw : requested) {
                if (raw == null || raw.isBlank()) continue;
                String campus = campusCatalogService.normalize(raw.trim());
                if (campus == null || campus.isBlank()) continue;
                adminAccessService.assertCanAccessCampus(campus);
                wanted.add(campus);
            }
        }

        var admin = adminAccessService.currentAdmin();
        List<String> allowed = adminAccessService.allowedCampusIds(admin);
        Set<String> existing = new LinkedHashSet<>(listCampusIds(userId));

        if (adminAccessService.isSuperAdmin(admin)) {
            for (String campus : new ArrayList<>(existing)) {
                if (!wanted.contains(campus)) {
                    userCampusRepo.deleteByUserIdAndCampusId(userId, campus);
                }
            }
            for (String campus : wanted) {
                ensureLinked(userId, campus);
            }
            return listCampusIds(userId);
        }

        // 校长：仅调整自己可管校区
        Set<String> allowedSet = new LinkedHashSet<>(allowed);
        for (String campus : allowedSet) {
            boolean shouldHave = wanted.contains(campus);
            boolean has = existing.contains(campus);
            if (shouldHave && !has) {
                ensureLinked(userId, campus);
            } else if (!shouldHave && has) {
                userCampusRepo.deleteByUserIdAndCampusId(userId, campus);
            }
        }
        return listCampusIds(userId);
    }

    /**
     * 将历史约课 / 签到中的校区回填到 user_campus，避免列表改为仅按关联后丢人。
     */
    @Transactional
    public int backfillFromActivity() {
        int created = 0;
        created += linkPairs(bookingRepo.findActiveUserCampusPairs());
        created += linkPairs(practiceRecordRepo.findUserCampusPairs());
        if (created > 0) {
            log.info("user_campus backfill created {} links", created);
        }
        return created;
    }

    private int linkPairs(List<Object[]> pairs) {
        int created = 0;
        if (pairs == null) {
            return 0;
        }
        for (Object[] pair : pairs) {
            if (pair == null || pair.length < 2 || pair[0] == null || pair[1] == null) {
                continue;
            }
            Long userId = pair[0] instanceof Number n ? n.longValue() : null;
            String campusId = String.valueOf(pair[1]).trim();
            if (userId == null || campusId.isBlank()) {
                continue;
            }
            if (!campusCatalogService.contains(campusId)) {
                continue;
            }
            String campus = campusCatalogService.normalize(campusId);
            if (userCampusRepo.existsByUserIdAndCampusId(userId, campus)) {
                continue;
            }
            UserCampus row = new UserCampus();
            row.setUserId(userId);
            row.setCampusId(campus);
            userCampusRepo.save(row);
            created++;
        }
        return created;
    }
}
