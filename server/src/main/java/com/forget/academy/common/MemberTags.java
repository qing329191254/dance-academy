package com.forget.academy.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 学员业务标签（后台人工标记，按校区） */
public final class MemberTags {
    public static final String FIXED_FEATURE = "fixed_feature";
    public static final String SESSION_CARD = "session_card";
    public static final String UNLIMITED_PASS = "unlimited_pass";
    public static final String PRIVATE_LESSON = "private_lesson";
    public static final String CUSTOM_COURSE = "custom_course";
    public static final String ROOM_RENTAL = "room_rental";
    public static final String PERFORMANCE = "performance";

    private static final Map<String, String> LABELS = new LinkedHashMap<>();

    static {
        LABELS.put(FIXED_FEATURE, "特色固定班");
        LABELS.put(SESSION_CARD, "次卡");
        LABELS.put(UNLIMITED_PASS, "通卡");
        LABELS.put(PRIVATE_LESSON, "私教");
        LABELS.put(CUSTOM_COURSE, "定制课");
        LABELS.put(ROOM_RENTAL, "教室租赁");
        LABELS.put(PERFORMANCE, "商演赛事");
    }

    private MemberTags() {
    }

    public static boolean isValid(String value) {
        return value != null && LABELS.containsKey(value);
    }

    public static String label(String code) {
        return LABELS.getOrDefault(code, code == null ? "" : code);
    }

    public static List<String> allKeys() {
        return List.copyOf(LABELS.keySet());
    }

    public static List<Map<String, String>> options() {
        return LABELS.entrySet().stream()
                .map(entry -> Map.of("value", entry.getKey(), "label", entry.getValue()))
                .toList();
    }
}
