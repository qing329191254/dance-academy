package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.ScheduleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final String STATUS_PENDING = "待上课";
    private static final String STATUS_WAITLIST = "排队中";
    private static final String STATUS_DONE = "已完成";
    private static final String STATUS_CANCELLED = "已取消";

    private final ScheduleRepo scheduleRepo;
    private final AdminAccessService adminAccessService;
    private final BookingRepo bookingRepo;
    private final AppUserRepo appUserRepo;
    private final BookingRemindService bookingRemindService;

    public List<Map<String, Object>> listSchedules(String type, String date, String campusId, Long userId) {
        String tab = type == null || type.isBlank() ? "group" : type;
        String campus = campusId == null || campusId.isBlank() ? CampusIds.DEFAULT : campusId.trim();
        List<Schedule> schedules;
        if ("group".equals(tab) && date != null && !date.isBlank()) {
            int weekday = toWeekday(LocalDate.parse(date));
            schedules = scheduleRepo.findByTypeAndWeekdayAndCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(
                    tab, weekday, campus);
        } else {
            schedules = scheduleRepo.findByTypeAndCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(tab, campus);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Schedule item : schedules) {
            Map<String, Object> row = toScheduleMap(item, date);
            long booked = 0;
            if ("group".equals(tab) && date != null) {
                booked = bookingRepo.countByScheduleIdAndClassDateAndStatus(item.getId(), date, STATUS_PENDING);
            }
            row.put("bookedCount", booked);
            row.put("status", resolveStatus(item, booked));
            row.put("booked", false);
            row.put("queued", false);
            if (userId != null) {
                String classDate = "group".equals(tab) ? date : "default";
                String key = buildKey(tab, item.getId(), classDate);
                bookingRepo.findByUserIdAndBookingKey(userId, key).ifPresent(booking -> {
                    if (STATUS_PENDING.equals(booking.getStatus())) {
                        row.put("booked", true);
                    } else if (STATUS_WAITLIST.equals(booking.getStatus())) {
                        row.put("queued", true);
                        row.put("status", STATUS_WAITLIST);
                        row.put("queueNo", queueNo(booking));
                    }
                });
            }
            result.add(row);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> toggle(Long userId, Long scheduleId, String date) {
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        String tab = schedule.getType();
        String classDate = "group".equals(tab) ? date : "default";
        if ("group".equals(tab) && (date == null || date.isBlank())) {
            throw new BizException("请选择上课日期");
        }
        String key = buildKey(tab, scheduleId, classDate);
        var existing = bookingRepo.findByUserIdAndBookingKey(userId, key);
        if (existing.isPresent() && STATUS_PENDING.equals(existing.get().getStatus())) {
            markCancelled(existing.get());
            promoteWaitlist(schedule, classDate);
            return result(false, false, "已取消预约", null);
        }
        if (existing.isPresent() && STATUS_WAITLIST.equals(existing.get().getStatus())) {
            markCancelled(existing.get());
            return result(false, false, "已退出排队", null);
        }
        if (existing.isPresent()) {
            Booking booking = existing.get();
            boolean full = isGroupFull(schedule, classDate);
            booking.setStatus(full ? STATUS_WAITLIST : STATUS_PENDING);
            booking.setRemindSent(false);
            booking.setNickname(appUserRepo.findById(userId).map(AppUser::getNickname).orElse(booking.getNickname()));
            bookingRepo.save(booking);
            if (full) {
                return queuedResult(booking);
            }
            bookingRemindService.scheduleGroupRemind(booking);
            return result(true, false, "预约成功", booking);
        }

        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        boolean waitlist = isGroupFull(schedule, classDate);
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setNickname(user.getNickname());
        booking.setScheduleId(scheduleId);
        booking.setBookingKey(key);
        booking.setTab(tab);
        booking.setClassDate("group".equals(tab) ? classDate : null);
        booking.setName(schedule.getName());
        booking.setTimeText(schedule.getTimeText());
        booking.setTeacherName(schedule.getTeacherName());
        booking.setRoom(schedule.getRoom());
        booking.setStatus(waitlist ? STATUS_WAITLIST : STATUS_PENDING);
        booking.setRemindSent(false);
        try {
            bookingRepo.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new BizException(waitlist ? "请勿重复排队" : "请勿重复预约");
        }
        if (waitlist) {
            return queuedResult(booking);
        }
        bookingRemindService.scheduleGroupRemind(booking);
        return result(true, false, "预约成功", booking);
    }

    @Transactional
    public Booking adminUpdateStatus(Long id, String status) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new BizException("预约不存在"));
        assertBookingCampusAccess(booking);
        if (status == null || status.isBlank() || status.equals(booking.getStatus())) {
            return booking;
        }
        String previous = booking.getStatus();
        if (STATUS_CANCELLED.equals(status)) {
            markCancelled(booking);
            if (STATUS_PENDING.equals(previous)) {
                scheduleRepo.findById(booking.getScheduleId())
                        .ifPresent(schedule -> promoteWaitlist(schedule, booking.getClassDate()));
            }
            return booking;
        }
        booking.setStatus(status);
        return bookingRepo.save(booking);
    }

    @Transactional
    public void adminDelete(Long id) {
        Booking booking = bookingRepo.findById(id).orElse(null);
        if (booking == null) {
            return;
        }
        assertBookingCampusAccess(booking);
        boolean wasPending = STATUS_PENDING.equals(booking.getStatus());
        Long scheduleId = booking.getScheduleId();
        String classDate = booking.getClassDate();
        bookingRepo.delete(booking);
        if (wasPending) {
            scheduleRepo.findById(scheduleId).ifPresent(schedule -> promoteWaitlist(schedule, classDate));
        }
    }

    public List<Map<String, Object>> myBookings(Long userId) {
        return bookingRepo.findByUserIdAndStatusInOrderByClassDateDescIdDesc(userId, List.of(STATUS_PENDING, STATUS_DONE))
                .stream()
                .map(this::toBookingMap)
                .toList();
    }

    public List<Map<String, Object>> myWaitlist(Long userId) {
        return bookingRepo.findByUserIdAndStatusOrderByClassDateAscIdAsc(userId, STATUS_WAITLIST)
                .stream()
                .map(this::toWaitlistMap)
                .toList();
    }

    public static String buildKey(String tab, Long scheduleId, String date) {
        return tab + ":" + (date == null || date.isBlank() ? "default" : date) + ":" + scheduleId;
    }

    private void markCancelled(Booking booking) {
        String key = booking.getBookingKey();
        if (key != null && !key.contains(":x:")) {
            booking.setBookingKey(key + ":x:" + booking.getId());
        }
        booking.setStatus(STATUS_CANCELLED);
        bookingRepo.save(booking);
    }

    private void assertBookingCampusAccess(Booking booking) {
        scheduleRepo.findById(booking.getScheduleId()).ifPresent(schedule ->
                adminAccessService.assertCanAccessCampus(schedule.getCampusId()));
    }

    private void promoteWaitlist(Schedule schedule, String classDate) {
        if (schedule == null || !"group".equals(schedule.getType()) || classDate == null || classDate.isBlank()) {
            return;
        }
        while (!isGroupFull(schedule, classDate)) {
            var next = bookingRepo.findFirstByScheduleIdAndClassDateAndStatusOrderByIdAsc(
                    schedule.getId(), classDate, STATUS_WAITLIST);
            if (next.isEmpty()) {
                return;
            }
            Booking booking = next.get();
            booking.setStatus(STATUS_PENDING);
            booking.setRemindSent(false);
            bookingRepo.save(booking);
            bookingRemindService.scheduleGroupRemind(booking);
        }
    }

    private boolean isGroupFull(Schedule schedule, String classDate) {
        if (!"group".equals(schedule.getType()) || classDate == null || classDate.isBlank() || "default".equals(classDate)) {
            return false;
        }
        long booked = bookingRepo.countByScheduleIdAndClassDateAndStatus(schedule.getId(), classDate, STATUS_PENDING);
        int capacity = schedule.getCapacity() == null ? 20 : schedule.getCapacity();
        return booked >= capacity;
    }

    private int queueNo(Booking booking) {
        if (booking.getId() == null) {
            long ahead = bookingRepo.countByScheduleIdAndClassDateAndStatus(
                    booking.getScheduleId(), booking.getClassDate(), STATUS_WAITLIST);
            return (int) ahead + 1;
        }
        return (int) bookingRepo.countByScheduleIdAndClassDateAndStatusAndIdLessThan(
                booking.getScheduleId(), booking.getClassDate(), STATUS_WAITLIST, booking.getId()) + 1;
    }

    private Map<String, Object> queuedResult(Booking booking) {
        Map<String, Object> map = result(false, true, "已加入排队，前面有人取消即可替补", booking);
        map.put("queueNo", queueNo(booking));
        return map;
    }

    private Map<String, Object> result(boolean booked, boolean queued, String message, Booking booking) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("booked", booked);
        map.put("queued", queued);
        map.put("message", message);
        if (booking != null) {
            map.put("booking", queued ? toWaitlistMap(booking) : toBookingMap(booking));
        }
        return map;
    }

    private Map<String, Object> toScheduleMap(Schedule item, String date) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("type", item.getType());
        map.put("name", item.getName());
        map.put("time", item.getTimeText());
        map.put("teacher", item.getTeacherName());
        map.put("teacherId", item.getTeacherId());
        map.put("room", item.getRoom());
        map.put("stars", item.getStars());
        map.put("weekday", item.getWeekday());
        map.put("capacity", item.getCapacity());
        map.put("date", date);
        return map;
    }

    public Map<String, Object> toBookingMap(Booking booking) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", booking.getId());
        map.put("key", booking.getBookingKey());
        map.put("tab", booking.getTab());
        map.put("scheduleId", booking.getScheduleId());
        map.put("name", booking.getName());
        map.put("date", booking.getClassDate());
        map.put("time", booking.getTimeText());
        map.put("teacher", booking.getTeacherName());
        map.put("room", booking.getRoom());
        map.put("status", booking.getStatus());
        return map;
    }

    private Map<String, Object> toWaitlistMap(Booking booking) {
        Map<String, Object> map = toBookingMap(booking);
        map.put("queueNo", queueNo(booking));
        return map;
    }

    private String resolveStatus(Schedule item, long booked) {
        int capacity = item.getCapacity() == null ? 20 : item.getCapacity();
        if (!"group".equals(item.getType())) {
            return item.getStatus() == null ? "可预约" : item.getStatus();
        }
        if (booked >= capacity) {
            return "已满";
        }
        if (booked >= Math.max(capacity - 3, capacity * 0.8)) {
            return "名额紧张";
        }
        return item.getStatus() == null ? "可预约" : item.getStatus();
    }

    /** 与 LocalDate.getDayOfWeek 对齐后转为 0=周日 */
    private int toWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SUNDAY ? 0 : day.getValue();
    }
}
