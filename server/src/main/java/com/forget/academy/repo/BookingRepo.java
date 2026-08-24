package com.forget.academy.repo;

import com.forget.academy.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndStatusNotOrderByClassDateDescIdDesc(Long userId, String status);

    Optional<Booking> findByUserIdAndBookingKey(Long userId, String bookingKey);

    long countByScheduleIdAndClassDateAndStatus(Long scheduleId, String classDate, String status);

    long countByScheduleIdAndStatus(Long scheduleId, String status);

    long countByClassDateAndStatus(String classDate, String status);

    Page<Booking> findByStatus(String status, Pageable pageable);

    Page<Booking> findByNameContainingOrNicknameContaining(String name, String nickname, Pageable pageable);

    @Query("""
            select b from Booking b
            where (:keyword = ''
                or lower(coalesce(b.name, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(b.nickname, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(b.teacherName, '')) like lower(concat('%', :keyword, '%')))
              and (:status = '' or b.status = :status)
            order by case when b.status = '待上课' then 0 when b.status = '已完成' then 1 else 2 end, b.id desc
            """)
    Page<Booking> search(@Param("keyword") String keyword,
                         @Param("status") String status,
                         Pageable pageable);

    @Query("""
            select b from Booking b
            where b.status = '待上课'
              and b.tab = 'group'
              and (b.remindSent is null or b.remindSent = false)
            """)
    List<Booking> findGroupPendingReminders();

    void deleteByUserId(Long userId);
}
