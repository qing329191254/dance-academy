package com.forget.academy.service;

import com.forget.academy.common.BizException;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final ScheduleRepo scheduleRepo;
    private final BookingRepo bookingRepo;
    private final AppUserRepo appUserRepo;
    private final WxSubscribeService wxSubscribeService;

    public List<Map<String, Object>> listSchedules(String type, String date, Long userId) {
        String tab = type == null || type.isBlank() ? "group" : type;
        List<Schedule> schedules;
        if ("group".equals(tab) && date != null && !date.isBlank()) {
            int weekday = toWeekday(LocalDate.parse(date));
            schedules = scheduleRepo.findByTypeAndWeekdayAndEnabledTrueOrderBySortOrderAscIdAsc(tab, weekday);
        } else {
            schedules = scheduleRepo.findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc(tab);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Schedule item : schedules) {
            Map<String, Object> row = toScheduleMap(item, date);
            long booked = 0;
            if ("group".equals(tab) && date != null) {
                booked = bookingRepo.countByScheduleIdAndClassDateAndStatus(item.getId(), date, "待上课");
            }
            row.put("bookedCount", booked);
            row.put("status", resolveStatus(item, booked));
            if (userId != null) {
                String classDate = "group".equals(tab) ? date : "default";
                String key = buildKey(tab, item.getId(), classDate);
                row.put("booked", bookingRepo.findByUserIdAndBookingKey(userId, key)
                        .filter(b -> "待上课".equals(b.getStatus()))
                        .isPresent());
            } else {
                row.put("booked", false);
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
        if (existing.isPresent() && "待上课".equals(existing.get().getStatus())) {
            Booking booking = existing.get();
            booking.setBookingKey(key + ":x:" + booking.getId());
            booking.setStatus("已取消");
            bookingRepo.save(booking);
            return Map.of("booked", false, "message", "已取消预约");
        }
        if (existing.isPresent()) {
            Booking booking = existing.get();
            booking.setStatus("待上课");
            booking.setNickname(appUserRepo.findById(userId).map(AppUser::getNickname).orElse(booking.getNickname()));
            bookingRepo.save(booking);
            return Map.of("booked", true, "message", "预约成功", "booking", toBookingMap(booking));
        }

        if ("group".equals(tab)) {
            long booked = bookingRepo.countByScheduleIdAndClassDateAndStatus(scheduleId, classDate, "待上课");
            int capacity = schedule.getCapacity() == null ? 20 : schedule.getCapacity();
            if (booked >= capacity) {
                throw new BizException("名额已满");
            }
        }

        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
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
        booking.setStatus("待上课");
        try {
            bookingRepo.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("请勿重复预约");
        }
        notifyBooked(user.getOpenid(), booking);
        return Map.of("booked", true, "message", "预约成功", "booking", toBookingMap(booking));
    }

    private void notifyBooked(String openid, Booking booking) {
        Runnable send = () -> wxSubscribeService.sendBookingNotice(openid, booking);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
            return;
        }
        send.run();
    }

    public List<Map<String, Object>> myBookings(Long userId) {
        return bookingRepo.findByUserIdAndStatusNotOrderByClassDateDescIdDesc(userId, "已取消")
                .stream()
                .map(this::toBookingMap)
                .toList();
    }

    public static String buildKey(String tab, Long scheduleId, String date) {
        return tab + ":" + (date == null || date.isBlank() ? "default" : date) + ":" + scheduleId;
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
