package com.forget.academy.common;

import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Schedule;

public final class ClosedClassGroup {
    public static final String ADVANCED = "advanced";
    public static final String FOUNDATION = "foundation";

    private ClosedClassGroup() {
    }

    public static boolean isValid(String value) {
        return ADVANCED.equals(value) || FOUNDATION.equals(value);
    }

    public static boolean isClosedDoor(Schedule schedule) {
        return schedule != null && Boolean.TRUE.equals(schedule.getClosedDoor());
    }

    public static boolean canAccess(Schedule schedule, AppUser user) {
        if (!isClosedDoor(schedule)) {
            return true;
        }
        if (user == null) {
            return false;
        }
        String audience = schedule.getAudienceGroup();
        if (audience == null || audience.isBlank()) {
            return false;
        }
        return audience.equals(user.getClosedClassGroup());
    }

    public static String label(String code) {
        if (ADVANCED.equals(code)) {
            return "高阶闭门";
        }
        if (FOUNDATION.equals(code)) {
            return "零基础闭门";
        }
        return "普通";
    }

    public static String accessDeniedMessage(Schedule schedule) {
        return "本节课为闭门课，仅面向「" + label(schedule.getAudienceGroup()) + "」学员开放";
    }
}
