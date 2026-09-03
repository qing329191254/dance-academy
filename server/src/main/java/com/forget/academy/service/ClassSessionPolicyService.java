package com.forget.academy.service;

import com.forget.academy.entity.Booking;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.ClassSessionCancelRepo;
import com.forget.academy.repo.ScheduleRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSessionPolicyService {
    private static final Logger log = LoggerFactory.getLogger(ClassSessionPolicyService.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    /** 开课前多久检查人数并可能取消 */
    public static final int CANCEL_LEAD_HOURS = 2;
    /** 开课后多久仍未签到则按缺席扣次 */
    public static final int NO_SHOW_GRACE_MINUTES = 60;

    private final ScheduleRepo scheduleRepo;
    private final BookingRepo bookingRepo;
    private final ClassSessionCancelRepo classSessionCancelRepo;
    private final BookingService bookingService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Shanghai")
    public void tick() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        try {
            checkLowEnrollment(now);
        } catch (Exception e) {
            log.warn("人数不足取消任务异常: {}", e.getMessage());
        }
        try {
            settleNoShows(now);
        } catch (Exception e) {
            log.warn("缺席扣次任务异常: {}", e.getMessage());
        }
    }

    void checkLowEnrollment(LocalDateTime now) {
        List<Schedule> schedules = scheduleRepo.findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc("group");
        LocalDate today = now.toLocalDate();
        for (int dayOffset = 0; dayOffset <= 1; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            int weekday = toWeekday(date);
            String classDate = date.toString();
            for (Schedule schedule : schedules) {
                if (schedule.getWeekday() == null || schedule.getWeekday() != weekday) {
                    continue;
                }
                int min = effectiveMinEnrollment(schedule);
                if (min <= 0) {
                    continue;
                }
                LocalDateTime start = ClassStartTimes.parse(classDate, schedule.getTimeText());
                if (start == null) {
                    continue;
                }
                LocalDateTime cancelAt = start.minusHours(CANCEL_LEAD_HOURS);
                // 已到开课前 2 小时节点，且尚未开课
                if (now.isBefore(cancelAt) || !now.isBefore(start)) {
                    continue;
                }
                if (classSessionCancelRepo.existsByScheduleIdAndClassDate(schedule.getId(), classDate)) {
                    continue;
                }
                long booked = bookingRepo.countByScheduleIdAndClassDateAndStatus(
                        schedule.getId(), classDate, "待上课");
                if (booked >= min) {
                    continue;
                }
                try {
                    bookingService.cancelSessionForLowEnrollment(schedule, classDate, (int) booked, min);
                    log.info("人数不足取消课程 scheduleId={} date={} booked={} min={}",
                            schedule.getId(), classDate, booked, min);
                } catch (Exception e) {
                    log.warn("取消课程失败 scheduleId={} date={}: {}", schedule.getId(), classDate, e.getMessage());
                }
            }
        }
    }

    void settleNoShows(LocalDateTime now) {
        List<Booking> pending = bookingRepo.findGroupPendingForNoShowSettle();
        for (Booking booking : pending) {
            if (Boolean.TRUE.equals(booking.getCardConsumed())) {
                continue;
            }
            // 已被人数不足取消的场次不应再扣（状态已是已取消，查不到）
            if (classSessionCancelRepo.existsByScheduleIdAndClassDate(
                    booking.getScheduleId(), booking.getClassDate())) {
                continue;
            }
            LocalDateTime start = ClassStartTimes.parse(booking.getClassDate(), booking.getTimeText());
            if (start == null) {
                continue;
            }
            if (now.isBefore(start.plusMinutes(NO_SHOW_GRACE_MINUTES))) {
                continue;
            }
            try {
                bookingService.settleNoShow(booking.getId());
            } catch (Exception e) {
                log.warn("缺席扣次失败 bookingId={}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    public static int effectiveMinEnrollment(Schedule schedule) {
        if (schedule == null || schedule.getMinEnrollment() == null) {
            return 4;
        }
        return schedule.getMinEnrollment();
    }

    /** 与 LocalDate.getDayOfWeek 对齐后转为 0=周日 */
    private int toWeekday(LocalDate date) {
        var day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SUNDAY ? 0 : day.getValue();
    }
}
