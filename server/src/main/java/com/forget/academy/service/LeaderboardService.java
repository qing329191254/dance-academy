package com.forget.academy.service;

import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.AppUser;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int TOP_SIZE = 20;

    private final PracticeRecordRepo practiceRecordRepo;
    private final AppUserRepo appUserRepo;

    public Map<String, Object> list(String period, String campusId, Long currentUserId) {
        String campus = campusId == null || campusId.isBlank() ? CampusIds.DEFAULT : campusId.trim();
        String fromDate = "month".equals(period)
                ? YearMonth.now(ZONE).atDay(1).toString()
                : "";
        List<Object[]> rows = practiceRecordRepo.rankByCheckin(campus, fromDate);
        List<Long> userIds = rows.stream()
                .map(row -> (Long) row[0])
                .filter(Objects::nonNull)
                .toList();
        Map<Long, AppUser> users = appUserRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, user -> user));

        List<Map<String, Object>> list = new ArrayList<>();
        Integer myRank = null;
        long myCount = 0;
        for (int i = 0; i < rows.size(); i++) {
            Long userId = (Long) rows.get(i)[0];
            long count = ((Number) rows.get(i)[1]).longValue();
            if (currentUserId != null && currentUserId.equals(userId)) {
                myRank = i + 1;
                myCount = count;
            }
            if (list.size() >= TOP_SIZE) {
                continue;
            }
            AppUser user = users.get(userId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", i + 1);
            row.put("nickname", displayName(user));
            row.put("avatar", user == null ? "" : nvl(user.getAvatar()));
            row.put("count", count);
            row.put("mine", currentUserId != null && currentUserId.equals(userId));
            list.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", fromDate.isBlank() ? "all" : "month");
        data.put("campusId", campus);
        data.put("list", list);
        data.put("mine", Map.of(
                "rank", myRank == null ? 0 : myRank,
                "count", myCount,
                "onBoard", myRank != null && myRank <= TOP_SIZE
        ));
        return data;
    }

    private static String displayName(AppUser user) {
        if (user == null) {
            return "舞室学员";
        }
        String name = user.getNickname();
        return name == null || name.isBlank() ? "舞室学员" : name.trim();
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }
}
