package com.forget.academy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 从课表 timeText（如 18:10-19:30）解析开课/下课时间。 */
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

    /** 下课时间；无法解析结束时刻时返回 null（不据此拦截约课）。 */
    public static LocalDateTime parseEnd(String classDate, String timeText) {
        if (classDate == null || classDate.isBlank() || "default".equals(classDate)) {
            return null;
        }
        LocalDate date;
        try {
            date = LocalDate.parse(classDate);
        } catch (Exception e) {
            return null;
        }
        List<LocalTime> clocks = clocks(timeText);
        if (clocks.size() < 2) {
            return null;
        }
        LocalTime start = clocks.get(0);
        LocalTime end = clocks.get(clocks.size() - 1);
        LocalDateTime endAt = LocalDateTime.of(date, end);
        if (end.isBefore(start)) {
            endAt = endAt.plusDays(1);
        }
        return endAt;
    }

    /** 当前是否已到下课时间（含整点下课）。无法解析下课时间则视为未结束。 */
    public static boolean isEnded(String classDate, String timeText, LocalDateTime now) {
        LocalDateTime end = parseEnd(classDate, timeText);
        if (end == null || now == null) {
            return false;
        }
        return !now.isBefore(end);
    }

    private static LocalTime startClock(String timeText) {
        List<LocalTime> clocks = clocks(timeText);
        return clocks.isEmpty() ? null : clocks.get(0);
    }

    private static List<LocalTime> clocks(String timeText) {
        List<LocalTime> result = new ArrayList<>();
        if (timeText == null || timeText.isBlank()) {
            return result;
        }
        Matcher matcher = CLOCK.matcher(timeText.trim());
        while (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            if (hour > 23 || minute > 59) {
                continue;
            }
            result.add(LocalTime.of(hour, minute));
        }
        return result;
    }
}
