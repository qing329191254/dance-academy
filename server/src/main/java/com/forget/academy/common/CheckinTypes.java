package com.forget.academy.common;

public final class CheckinTypes {
    public static final String CLASS = "class";
    public static final String DUTY = "duty";
    public static final String TEACHER = "teacher";

    private CheckinTypes() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return CLASS;
        }
        String key = value.trim().toLowerCase();
        return switch (key) {
            case DUTY -> DUTY;
            case TEACHER -> TEACHER;
            default -> CLASS;
        };
    }

    public static String label(String checkinType) {
        return switch (normalize(checkinType)) {
            case DUTY -> "值班";
            case TEACHER -> "教师考勤";
            default -> "上课";
        };
    }
}
