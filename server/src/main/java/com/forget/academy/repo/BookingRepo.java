package com.forget.academy.repo;

import com.forget.academy.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndStatusNotOrderByClassDateDescIdDesc(Long userId, String status);

    Optional<Booking> findByUserIdAndBookingKey(Long userId, String bookingKey);

    long countByScheduleIdAndClassDateAndStatus(Long scheduleId, String classDate, String status);

    long countByClassDateAndStatus(String classDate, String status);

    Page<Booking> findByStatus(String status, Pageable pageable);

    Page<Booking> findByNameContainingOrNicknameContaining(String name, String nickname, Pageable pageable);

    void deleteByUserId(Long userId);
}
