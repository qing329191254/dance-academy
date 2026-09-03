package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.UserCard;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.UserCardRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCardExpireModeTest {

    @Mock
    private UserCardRepo userCardRepo;
    @Mock
    private BookingRepo bookingRepo;
    @Mock
    private ScheduleRepo scheduleRepo;
    @Mock
    private DanceCategoryService danceCategoryService;

    private UserCardService service;
    private final LocalDate today = LocalDate.of(2026, 9, 3);

    @BeforeEach
    void setUp() {
        service = new UserCardService(userCardRepo, bookingRepo, scheduleRepo, danceCategoryService);
    }

    @Test
    void resolveMode_defaultsToFromActivation() {
        UserCard card = new UserCard();
        assertEquals(UserCardService.MODE_FROM_ACTIVATION, UserCardService.resolveExpireMode(card));
        assertEquals(UserCardService.MODE_FROM_ACTIVATION, UserCardService.resolveExpireMode(null));
    }

    @Test
    void normalize_fixedRequiresExpireDate() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        assertThrows(BizException.class, () -> service.normalizeExpireFields(card));
    }

    @Test
    void normalize_fixedClearsValidDays() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        card.setValidDays(60);
        card.setExpireDate(LocalDate.of(2026, 12, 31));
        service.normalizeExpireFields(card);
        assertNull(card.getValidDays());
        assertEquals(UserCardService.MODE_FIXED_DEADLINE, card.getExpireMode());
    }

    @Test
    void normalize_fromActivationClearsExpireWhenNotActivated() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FROM_ACTIVATION);
        card.setValidDays(60);
        card.setExpireDate(LocalDate.of(2026, 12, 31));
        service.normalizeExpireFields(card);
        assertNull(card.getExpireDate());
        assertEquals(60, card.getValidDays());
    }

    @Test
    void expired_fromActivation_unactivatedIgnoresExpireDate() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FROM_ACTIVATION);
        card.setExpireDate(LocalDate.of(2026, 1, 1));
        assertFalse(service.isExpired(card, today));
        assertTrue(service.isUsable(withRemain(card, 5), null, today));
    }

    @Test
    void expired_fromActivation_activatedPastExpire() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FROM_ACTIVATION);
        card.setActivatedAt(LocalDate.of(2026, 1, 1));
        card.setExpireDate(LocalDate.of(2026, 3, 1));
        card.setRemain(3);
        assertTrue(service.isExpired(card, today));
        assertFalse(service.isUsable(card, null, today));
    }

    @Test
    void expired_fixedDeadline_unactivatedPastDeadlineIsVoid() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        card.setExpireDate(LocalDate.of(2026, 8, 31));
        card.setRemain(10);
        assertTrue(service.isExpired(card, today));
        assertFalse(service.isUsable(card, null, today));
    }

    @Test
    void expired_fixedDeadline_beforeDeadlineStillUsable() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        card.setExpireDate(LocalDate.of(2026, 12, 31));
        card.setRemain(10);
        assertFalse(service.isExpired(card, today));
        assertTrue(service.isUsable(card, null, today));
    }

    @Test
    void expired_sameDayAsExpireDate_notExpired() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        card.setExpireDate(today);
        card.setRemain(1);
        assertFalse(service.isExpired(card, today));
    }

    @Test
    void consume_fromActivation_setsExpireOnFirstUse() {
        UserCard card = new UserCard();
        card.setId(1L);
        card.setExpireMode(UserCardService.MODE_FROM_ACTIVATION);
        card.setValidDays(30);
        card.setRemain(5);
        when(userCardRepo.findById(1L)).thenReturn(Optional.of(card));
        when(userCardRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.consume(card, today);

        assertEquals(today, card.getActivatedAt());
        assertEquals(today.plusDays(30), card.getExpireDate());
        assertEquals(4, card.getRemain());
        verify(userCardRepo).save(card);
    }

    @Test
    void consume_fixedDeadline_keepsExpireDateOnActivate() {
        LocalDate deadline = LocalDate.of(2026, 12, 31);
        UserCard card = new UserCard();
        card.setId(2L);
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        card.setExpireDate(deadline);
        card.setRemain(8);
        when(userCardRepo.findById(2L)).thenReturn(Optional.of(card));
        when(userCardRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.consume(card, today);

        assertEquals(today, card.getActivatedAt());
        assertEquals(deadline, card.getExpireDate());
        assertEquals(7, card.getRemain());
    }

    @Test
    void consume_fixedDeadline_rejectsOverdueUnactivated() {
        UserCard card = new UserCard();
        card.setId(3L);
        card.setExpireMode(UserCardService.MODE_FIXED_DEADLINE);
        card.setExpireDate(LocalDate.of(2026, 8, 1));
        card.setRemain(5);
        when(userCardRepo.findById(3L)).thenReturn(Optional.of(card));

        BizException ex = assertThrows(BizException.class, () -> service.consume(card, today));
        assertEquals("次卡已逾期作废", ex.getMessage());
    }

    @Test
    void sectionMismatch_blocksUsable() {
        UserCard card = new UserCard();
        card.setExpireMode(UserCardService.MODE_FROM_ACTIVATION);
        card.setRemain(5);
        card.setSectionId(10L);
        assertFalse(service.isUsable(card, 20L, today));
        assertTrue(service.isUsable(card, 10L, today));
        assertTrue(service.isUsable(card, null, today));
    }

    private static UserCard withRemain(UserCard card, int remain) {
        card.setRemain(remain);
        return card;
    }
}
