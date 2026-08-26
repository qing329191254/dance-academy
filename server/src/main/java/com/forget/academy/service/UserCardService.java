package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.UserCard;
import com.forget.academy.repo.UserCardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCardService {
    public static final String GROUP_CARD_TYPE = "团课";

    private final UserCardRepo userCardRepo;

    public UserCard findUsableGroupCard(Long userId) {
        if (userId == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        List<UserCard> cards = userCardRepo.findByUserIdOrderByIdDesc(userId);
        for (UserCard card : cards) {
            if (!GROUP_CARD_TYPE.equals(card.getType())) {
                continue;
            }
            if (card.getRemain() == null || card.getRemain() <= 0) {
                continue;
            }
            if (card.getExpireDate() != null && card.getExpireDate().isBefore(today)) {
                continue;
            }
            return card;
        }
        return null;
    }

    public UserCard requireUsableGroupCard(Long userId) {
        UserCard card = findUsableGroupCard(userId);
        if (card == null) {
            throw new BizException("团课卡次数不足或已过期，请先联系前台办卡");
        }
        return card;
    }

    @Transactional
    public void deduct(UserCard card) {
        if (card == null || card.getId() == null) {
            throw new BizException("团课卡不存在");
        }
        UserCard latest = userCardRepo.findById(card.getId()).orElseThrow(() -> new BizException("团课卡不存在"));
        if (latest.getRemain() == null || latest.getRemain() <= 0) {
            throw new BizException("团课卡次数不足");
        }
        latest.setRemain(latest.getRemain() - 1);
        userCardRepo.save(latest);
    }

    @Transactional
    public void refund(Long cardId) {
        if (cardId == null) {
            return;
        }
        userCardRepo.findById(cardId).ifPresent(card -> {
            if (!GROUP_CARD_TYPE.equals(card.getType())) {
                return;
            }
            int remain = card.getRemain() == null ? 0 : card.getRemain();
            card.setRemain(remain + 1);
            userCardRepo.save(card);
        });
    }
}
