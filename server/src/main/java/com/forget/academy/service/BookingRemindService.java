package com.forget.academy.service;

import com.forget.academy.entity.Booking;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BookingRemindService {
    private static final Logger log = LoggerFactory.getLogger(BookingRemindService.class);
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int LEAD_MINUTES = 30;
    private static final int GRACE_MINUTES = 15;

    private final BookingRepo bookingRepo;
    private final AppUserRepo appUserRepo;
    private final WxSubscribeService wxSubscribeService;

    void scheduleGroupRemind(Booking booking) {
        if (booking == null || booking.getId() == null || !"group".equals(booking.getTab())) {
            return;
        }
        Long id = booking.getId();
        Runnable run = () -> trySend(id);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    run.run();
                }
            });
            return;
        }
        run.run();
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Shanghai")
    public void sendDueGroupReminders() {
        for (Booking booking : bookingRepo.findGroupPendingReminders()) {
            trySend(booking);
        }
    }

    public void trySend(Long bookingId) {
        bookingRepo.findById(bookingId).ifPresent(this::trySend);
    }

    private void trySend(Booking booking) {
        if (!"group".equals(booking.getTab()) || !"待上课".equals(booking.getStatus())) {
            return;
        }
        if (Boolean.TRUE.equals(booking.getRemindSent())) {
            return;
        }
        LocalDateTime start = ClassStartTimes.parse(booking.getClassDate(), booking.getTimeText());
        if (start == null) {
            log.warn("团课预约无法解析开课时间，跳过提醒 bookingId={}", booking.getId());
            markSent(booking);
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZONE);
        if (now.isBefore(start.minusMinutes(LEAD_MINUTES))) {
            return;
        }
        if (now.isAfter(start.plusMinutes(GRACE_MINUTES))) {
            markSent(booking);
            return;
        }
        String openid = appUserRepo.findById(booking.getUserId()).map(u -> u.getOpenid()).orElse("");
        if (wxSubscribeService.sendBookingNotice(openid, booking)) {
            markSent(booking);
        }
    }

    private void markSent(Booking booking) {
        booking.setRemindSent(true);
        bookingRepo.save(booking);
    }
}
