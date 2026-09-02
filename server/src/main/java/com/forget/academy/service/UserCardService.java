package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.UserCard;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.UserCardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserCardService {
    public static final String GROUP_CARD_TYPE = "团课";
    public static final String FIXED_CARD_TYPE = "固定班";
    public static final String PRIVATE_CARD_TYPE = "私教";

    private final UserCardRepo userCardRepo;
    private final BookingRepo bookingRepo;
    private final ScheduleRepo scheduleRepo;
    private final DanceCategoryService danceCategoryService;

    public static String cardTypeForSchedule(String scheduleType) {
        if (scheduleType == null) {
            return GROUP_CARD_TYPE;
        }
        return switch (scheduleType.trim().toLowerCase()) {
            case "fixed" -> FIXED_CARD_TYPE;
            case "private" -> PRIVATE_CARD_TYPE;
            default -> GROUP_CARD_TYPE;
        };
    }

    public UserCard findUsableCard(Long userId, String cardType, Long scheduleSectionId) {
        if (userId == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        String type = cardType == null || cardType.isBlank() ? GROUP_CARD_TYPE : cardType.trim();
        List<UserCard> cards = userCardRepo.findByUserIdOrderByIdDesc(userId);
        for (UserCard card : cards) {
            if (!type.equals(card.getType())) {
                continue;
            }
            if (!isUsable(card, scheduleSectionId, today)) {
                continue;
            }
            return card;
        }
        return null;
    }

    public UserCard findUsableGroupCard(Long userId) {
        return findUsableCard(userId, GROUP_CARD_TYPE, null);
    }

    public UserCard findUsableGroupCard(Long userId, Long scheduleSectionId) {
        return findUsableCard(userId, GROUP_CARD_TYPE, scheduleSectionId);
    }

    public UserCard requireUsableGroupCard(Long userId) {
        return requireUsableGroupCard(userId, null);
    }

    public UserCard requireUsableGroupCard(Long userId, Long scheduleSectionId) {
        UserCard card = findUsableGroupCard(userId, scheduleSectionId);
        if (card == null) {
            throw new BizException(blockReason(userId, GROUP_CARD_TYPE, scheduleSectionId));
        }
        return card;
    }

    public String blockReason(Long userId, String cardType, Long scheduleSectionId) {
        if (userId == null) {
            return "请先登录";
        }
        String type = cardType == null ? GROUP_CARD_TYPE : cardType;
        List<UserCard> cards = userCardRepo.findByUserIdOrderByIdDesc(userId).stream()
                .filter(c -> type.equals(c.getType()))
                .toList();
        if (cards.isEmpty()) {
            return type + "卡次数不足，请先联系前台办卡";
        }
        boolean anyRemain = cards.stream().anyMatch(c -> c.getRemain() != null && c.getRemain() > 0);
        if (!anyRemain) {
            return type + "卡次数已用完，请联系前台办卡";
        }
        LocalDate today = LocalDate.now();
        boolean anyNotExpired = cards.stream().anyMatch(c ->
                c.getRemain() != null && c.getRemain() > 0 && !isExpired(c, today));
        if (!anyNotExpired) {
            return type + "卡已过期，请联系前台办卡";
        }
        if (scheduleSectionId != null) {
            boolean sectionMismatch = cards.stream().anyMatch(c ->
                    c.getRemain() != null && c.getRemain() > 0
                            && !isExpired(c, today)
                            && c.getSectionId() != null
                            && !Objects.equals(c.getSectionId(), scheduleSectionId));
            if (sectionMismatch) {
                String sectionName = danceCategoryService.nameOf(scheduleSectionId);
                return "该课属于「" + (sectionName == null ? "指定板块" : sectionName)
                        + "」，您没有可用的对应次卡，请联系前台办卡";
            }
        }
        return type + "卡次数不足或已过期，请先联系前台办卡";
    }

    public boolean isUsable(UserCard card, Long scheduleSectionId, LocalDate today) {
        if (card == null) {
            return false;
        }
        if (card.getRemain() == null || card.getRemain() <= 0) {
            return false;
        }
        if (isExpired(card, today)) {
            return false;
        }
        if (card.getSectionId() != null && scheduleSectionId != null
                && !Objects.equals(card.getSectionId(), scheduleSectionId)) {
            return false;
        }
        return true;
    }

    private boolean isExpired(UserCard card, LocalDate today) {
        // 未开卡：不按到期日拦截
        if (card.getActivatedAt() == null) {
            return false;
        }
        return card.getExpireDate() != null && card.getExpireDate().isBefore(today);
    }

    @Transactional
    public void deduct(UserCard card) {
        consume(card, LocalDate.now());
    }

    @Transactional
    public void consume(UserCard card, LocalDate classDate) {
        if (card == null || card.getId() == null) {
            throw new BizException("次卡不存在");
        }
        UserCard latest = userCardRepo.findById(card.getId()).orElseThrow(() -> new BizException("次卡不存在"));
        if (latest.getRemain() == null || latest.getRemain() <= 0) {
            throw new BizException("次卡次数不足");
        }
        LocalDate day = classDate == null ? LocalDate.now() : classDate;
        if (latest.getActivatedAt() == null) {
            latest.setActivatedAt(day);
            if (latest.getValidDays() != null && latest.getValidDays() > 0) {
                latest.setExpireDate(day.plusDays(latest.getValidDays()));
            }
        } else if (isExpired(latest, day)) {
            throw new BizException("次卡已过期");
        }
        latest.setRemain(latest.getRemain() - 1);
        userCardRepo.save(latest);
    }

    /**
     * 到课成功后：扣锁定的卡（或补找可用卡），并标记预约已消耗。
     */
    @Transactional
    public void consumeOnClassCheckin(Long userId, Long scheduleId, String classDate) {
        if (userId == null || scheduleId == null) {
            return;
        }
        String rawDate = classDate == null || classDate.isBlank() || "default".equals(classDate.trim())
                ? LocalDate.now().toString()
                : classDate.trim();
        LocalDate day;
        try {
            day = LocalDate.parse(rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate);
        } catch (Exception e) {
            day = LocalDate.now();
        }
        String lookupDate = day.toString();
        Booking booking = bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                        userId, scheduleId, lookupDate, "待上课")
                .or(() -> bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                        userId, scheduleId, lookupDate, "已完成"))
                .orElse(null);
        if (booking == null && !"default".equals(classDate)) {
            booking = bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                            userId, scheduleId, "default", "待上课")
                    .or(() -> bookingRepo.findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
                            userId, scheduleId, "default", "已完成"))
                    .orElse(null);
        }
        if (booking == null) {
            return;
        }
        if (Boolean.TRUE.equals(booking.getCardConsumed())) {
            return;
        }
        Schedule schedule = scheduleRepo.findById(scheduleId).orElse(null);
        String cardType = cardTypeForSchedule(schedule == null ? booking.getTab() : schedule.getType());
        Long sectionId = schedule == null ? null : schedule.getSectionId();
        UserCard card = null;
        if (booking.getCardId() != null) {
            card = userCardRepo.findById(booking.getCardId()).orElse(null);
        }
        if (card == null || !isUsable(card, sectionId, day)) {
            card = findUsableCard(userId, cardType, sectionId);
            if (card != null) {
                booking.setCardId(card.getId());
            }
        }
        if (card == null) {
            booking.setCardConsumed(true);
            booking.setStatus("已完成");
            bookingRepo.save(booking);
            return;
        }
        consume(card, day);
        booking.setCardConsumed(true);
        booking.setStatus("已完成");
        bookingRepo.save(booking);
    }

    @Transactional
    public void refund(Long cardId) {
        if (cardId == null) {
            return;
        }
        userCardRepo.findById(cardId).ifPresent(card -> {
            int remain = card.getRemain() == null ? 0 : card.getRemain();
            card.setRemain(remain + 1);
            userCardRepo.save(card);
        });
    }

    public Map<String, Object> toPublicMap(UserCard card) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", card.getId());
        map.put("name", card.getName());
        map.put("type", card.getType());
        map.put("remain", card.getRemain());
        map.put("total", card.getTotal());
        map.put("sectionId", card.getSectionId());
        map.put("sectionName", card.getSectionName() != null
                ? card.getSectionName()
                : danceCategoryService.nameOf(card.getSectionId()));
        map.put("validDays", card.getValidDays());
        map.put("activatedAt", card.getActivatedAt());
        map.put("activated", card.getActivatedAt() != null);
        map.put("expireDate", card.getExpireDate());
        map.put("cover", card.getCover());
        return map;
    }
}
