package com.forget.academy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 从课表 timeText（如 18:10-19:30）解析开课开始时间。 */
public final class ClassStartTimes {
    private static final Pattern CLOCK = Pattern.compile("(\\d{1,2}):(\\d{2})");

    private ClassStartTimes() {
    }

    public static LocalDateTime parse(String classDate, String timeText) {
        if (classDate == null || classDate.isBlank() || "default".equals(classDate)) {
            return null;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(classDate);
        } catch (Exception e) {
            return null;
        }
        LocalTime clock = startClock(timeText);
        if (clock == null) {
            return null;
        }
        return LocalDateTime.of(date, clock);
    }

    private static LocalTime startClock(String timeText) {
        if (timeText == null || timeText.isBlank()) {
            return null;
        }
        Matcher matcher = CLOCK.matcher(timeText.trim());
        if (!matcher.find()) {
            return null;
        }
        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        if (hour > 23 || minute > 59) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }
}
