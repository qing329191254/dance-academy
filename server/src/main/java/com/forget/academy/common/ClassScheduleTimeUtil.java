package com.forget.academy.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClassScheduleTimeUtil {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern START_TIME = Pattern.compile("(\\d{1,2}:\\d{2})");

    private ClassScheduleTimeUtil() {
    }

    public static LocalDateTime classStartAt(String classDate, String timeText) {
        String date = classDate == null || classDate.isBlank()
                ? LocalDate.now(ZONE).toString()
                : classDate.trim();
        String start = parseStartTime(timeText);
        return LocalDateTime.parse(date + "T" + start, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'H:mm"));
    }

    public static int minutesLate(LocalDateTime deadline, LocalDateTime actual) {
        if (!actual.isAfter(deadline)) {
            return 0;
        }
        return (int) java.time.Duration.between(deadline, actual).toMinutes();
    }

    private static String parseStartTime(String timeText) {
        if (timeText == null || timeText.isBlank()) {
            return "00:00";
        }
        Matcher matcher = START_TIME.matcher(timeText.trim());
        if (!matcher.find()) {
            return "00:00";
        }
        String[] parts = matcher.group(1).split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return String.format("%02d:%02d", hour, minute);
    }
}
