package com.forget.academy.service;

import com.forget.academy.entity.Classroom;
import com.forget.academy.entity.ClassroomSlot;
import com.forget.academy.entity.RoomRental;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.ClassroomRepo;
import com.forget.academy.repo.RoomRentalRepo;
import com.forget.academy.repo.ScheduleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
    private static final Pattern TIME_RANGE = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*[-~–—]\\s*(\\d{1,2}):(\\d{2})");
    public static final String RENTAL_CONFIRMED = "confirmed";

    private final ClassroomRepo classroomRepo;
    private final ScheduleRepo scheduleRepo;
    private final RoomRentalRepo roomRentalRepo;

    public boolean isOccupiedByClassOrRental(Long classroomId, String date, String startTime, String endTime) {
        Classroom classroom = classroomRepo.findById(classroomId).orElse(null);
        if (classroom == null) {
            return true;
        }
        int slotStart = minutes(startTime);
        int slotEnd = minutes(endTime);
        if (slotEnd <= slotStart) {
            return true;
        }
        if (occupiedByRental(classroomId, date, slotStart, slotEnd)) {
            return true;
        }
        return occupiedBySchedule(classroom, date, slotStart, slotEnd);
    }

    private boolean occupiedByRental(Long classroomId, String date, int slotStart, int slotEnd) {
        List<RoomRental> rentals = roomRentalRepo.findByClassroomIdAndClassDateAndStatus(
                classroomId, date, RENTAL_CONFIRMED);
        for (RoomRental rental : rentals) {
            if (overlap(slotStart, slotEnd, minutes(rental.getStartTime()), minutes(rental.getEndTime()))) {
                return true;
            }
        }
        return false;
    }

    private boolean occupiedBySchedule(Classroom classroom, String date, int slotStart, int slotEnd) {
        LocalDate day;
        try {
            day = LocalDate.parse(date);
        } catch (Exception e) {
            return true;
        }
        int weekday = day.getDayOfWeek().getValue() % 7;
        String campus = classroom.getCampusId();
        for (Schedule schedule : scheduleRepo.findByCampusIdAndEnabledTrue(campus)) {
            if (!roomMatches(classroom, schedule.getRoom())) {
                continue;
            }
            String type = schedule.getType() == null ? "" : schedule.getType();
            if ("private".equals(type)) {
                continue;
            }
            if ("group".equals(type) || "fixed".equals(type)) {
                if (schedule.getWeekday() != null && schedule.getWeekday() != weekday) {
                    continue;
                }
                if ("group".equals(type) && schedule.getWeekday() == null) {
                    continue;
                }
            }
            int[] range = parseTimeRange(schedule.getTimeText());
            if (overlap(slotStart, slotEnd, range[0], range[1])) {
                return true;
            }
        }
        return false;
    }

    private static boolean roomMatches(Classroom classroom, String room) {
        if (room == null || room.isBlank()) {
            return false;
        }
        String value = room.trim();
        if (value.equals(classroom.getName())) {
            return true;
        }
        return classroom.getShortName() != null && !classroom.getShortName().isBlank()
                && value.equals(classroom.getShortName().trim());
    }

    public static int[] parseTimeRange(String text) {
        if (text == null || text.isBlank()) {
            return new int[]{0, 24 * 60};
        }
        Matcher matcher = TIME_RANGE.matcher(text);
        if (!matcher.find()) {
            return new int[]{0, 24 * 60};
        }
        int start = Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
        int end = Integer.parseInt(matcher.group(3)) * 60 + Integer.parseInt(matcher.group(4));
        if (end <= start) {
            return new int[]{0, 24 * 60};
        }
        return new int[]{start, end};
    }

    public static boolean overlap(int startA, int endA, int startB, int endB) {
        return startA < endB && startB < endA;
    }

    public static int minutes(String hhmm) {
        if (hhmm == null || !hhmm.contains(":")) {
            return 0;
        }
        String[] parts = hhmm.trim().split(":");
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String normalizeHm(String value) {
        int total = minutes(value);
        int hour = Math.floorDiv(total, 60);
        int minute = Math.floorMod(total, 60);
        return String.format("%02d:%02d", hour, minute);
    }
}
