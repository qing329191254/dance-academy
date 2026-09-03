package com.forget.academy.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassSessionPolicyLogicTest {

    @Test
    void parseStartTime() {
        LocalDateTime start = ClassStartTimes.parse("2026-09-09", "18:10-19:30");
        assertEquals(LocalDateTime.of(2026, 9, 9, 18, 10), start);
        assertNull(ClassStartTimes.parse(null, "18:10"));
        assertNull(ClassStartTimes.parse("default", "18:10"));
    }

    @Test
    void cancelWindow_twoHoursBefore() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 9, 18, 10);
        LocalDateTime cancelAt = start.minusHours(ClassSessionPolicyService.CANCEL_LEAD_HOURS);
        assertEquals(LocalDateTime.of(2026, 9, 9, 16, 10), cancelAt);
        assertTrue(LocalDateTime.of(2026, 9, 9, 16, 11).isAfter(cancelAt)
                || !LocalDateTime.of(2026, 9, 9, 16, 11).isBefore(cancelAt));
    }

    @Test
    void effectiveMinEnrollment_defaultsToFour() {
        assertEquals(4, ClassSessionPolicyService.effectiveMinEnrollment(null));
        var s = new com.forget.academy.entity.Schedule();
        assertEquals(4, ClassSessionPolicyService.effectiveMinEnrollment(s));
        s.setMinEnrollment(0);
        assertEquals(0, ClassSessionPolicyService.effectiveMinEnrollment(s));
        s.setMinEnrollment(6);
        assertEquals(6, ClassSessionPolicyService.effectiveMinEnrollment(s));
    }
}
