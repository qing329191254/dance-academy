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
    /** 首次到课起算有效天数 */
    public static final String MODE_FROM_ACTIVATION = "from_activation";
    /** 固定截止日期，逾期未开卡也作废 */
    public static final String MODE_FIXED_DEADLINE = "fixed_deadline";

    private final UserCardRepo userCardRepo;
    private final BookingRepo bookingRepo;
    private final ScheduleRepo scheduleRepo;
    private final DanceCategoryService danceCategoryService;

    public static String resolveExpireMode(UserCard card) {
        if (card == null) {
            return MODE_FROM_ACTIVATION;
        }
        String mode = card.getExpireMode();
        if (MODE_FIXED_DEADLINE.equals(mode) || MODE_FROM_ACTIVATION.equals(mode)) {
            return mode;
        }
        // 旧数据：未标明模式时按「首次到课起算」兼容
        return MODE_FROM_ACTIVATION;
    }

    /** 发卡/改卡时规范字段：二选一，互斥清理 */
    public void normalizeExpireFields(UserCard card) {
        if (card == null) {
            return;
        }
        String mode = resolveExpireMode(card);
        card.setExpireMode(mode);
        if (MODE_FIXED_DEADLINE.equals(mode)) {
            card.setValidDays(null);
            if (card.getExpireDate() == null) {
                throw new BizException("固定截止日期模式下请填写到期日");
            }
        } else {
            if (card.getValidDays() != null && card.getValidDays() <= 0) {
                card.setValidDays(null);
            }
            // 未开卡时不应提前写死到期日（由首次到课写入）
            if (card.getActivatedAt() == null) {
                card.setExpireDate(null);
            }
        }
    }

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
            boolean anyDeadlineMissed = cards.stream().anyMatch(c ->
                    c.getRemain() != null && c.getRemain() > 0
                            && MODE_FIXED_DEADLINE.equals(resolveExpireMode(c))
                            && c.getExpireDate() != null
                            && c.getExpireDate().isBefore(today)
                            && c.getActivatedAt() == null);
            if (anyDeadlineMissed) {
                return type + "卡已逾期作废（未在截止日期前开卡），请联系前台办卡";
            }
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

    public boolean isExpired(UserCard card, LocalDate today) {
        if (card == null || card.getExpireDate() == null || today == null) {
            return false;
        }
        if (!card.getExpireDate().isBefore(today)) {
            return false;
        }
        String mode = resolveExpireMode(card);
        // 固定截止：到期即作废（含未开卡逾期）
        if (MODE_FIXED_DEADLINE.equals(mode)) {
            return true;
        }
        // 首次到课起算：未开卡不算过期；开卡后看到期日
        return card.getActivatedAt() != null;
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
        if (isExpired(latest, day)) {
            throw new BizException(MODE_FIXED_DEADLINE.equals(resolveExpireMode(latest)) && latest.getActivatedAt() == null
                    ? "次卡已逾期作废"
                    : "次卡已过期");
        }
        if (latest.getActivatedAt() == null) {
            latest.setActivatedAt(day);
            // 仅「首次到课起算」在开卡时写入到期日；固定截止模式沿用发卡时的到期日
            if (MODE_FROM_ACTIVATION.equals(resolveExpireMode(latest))
                    && latest.getValidDays() != null
                    && latest.getValidDays() > 0) {
                latest.setExpireDate(day.plusDays(latest.getValidDays()));
            }
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
        LocalDate today = LocalDate.now();
        String mode = resolveExpireMode(card);
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
        map.put("expireMode", mode);
        map.put("validDays", card.getValidDays());
        map.put("activatedAt", card.getActivatedAt());
        map.put("activated", card.getActivatedAt() != null);
        map.put("expireDate", card.getExpireDate());
        map.put("expired", isExpired(card, today));
        map.put("cover", card.getCover());
        return map;
    }
}
