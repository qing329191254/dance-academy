package com.forget.academy.repo;

import com.forget.academy.entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndStatusNotOrderByClassDateDescIdDesc(Long userId, String status);

    List<Booking> findByUserIdAndStatusInOrderByClassDateDescIdDesc(Long userId, Collection<String> statuses);

    List<Booking> findByUserIdAndStatusOrderByClassDateAscIdAsc(Long userId, String status);

    Optional<Booking> findByUserIdAndBookingKey(Long userId, String bookingKey);

    long countByScheduleIdAndClassDateAndStatus(Long scheduleId, String classDate, String status);

    List<Booking> findByScheduleIdAndClassDateAndStatusOrderByIdAsc(
            Long scheduleId, String classDate, String status);

    Optional<Booking> findFirstByUserIdAndScheduleIdAndClassDateAndStatus(
            Long userId, Long scheduleId, String classDate, String status);

    @Query("""
            select count(b) from Booking b join Schedule s on b.scheduleId = s.id
            where b.classDate = :classDate and b.status = :status and s.campusId in :campusIds
            """)
    long countByClassDateAndStatusInCampuses(@Param("classDate") String classDate,
                                             @Param("status") String status,
                                             @Param("campusIds") List<String> campusIds);

    @Query("""
            select b from Booking b join Schedule s on b.scheduleId = s.id
            where s.campusId in :campusIds
            order by b.id desc
            """)
    Page<Booking> findLatestInCampuses(@Param("campusIds") List<String> campusIds, Pageable pageable);

    long countByScheduleIdAndClassDateAndStatusAndIdLessThan(Long scheduleId, String classDate, String status, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Booking> findFirstByScheduleIdAndClassDateAndStatusOrderByIdAsc(Long scheduleId, String classDate, String status);

    long countByScheduleIdAndStatus(Long scheduleId, String status);

    long countByClassDateAndStatus(String classDate, String status);

    Page<Booking> findByStatus(String status, Pageable pageable);

    Page<Booking> findByNameContainingOrNicknameContaining(String name, String nickname, Pageable pageable);

    @Query("""
            select b from Booking b join Schedule s on b.scheduleId = s.id
            where (:keyword = ''
                or lower(coalesce(b.name, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(b.nickname, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(b.teacherName, '')) like lower(concat('%', :keyword, '%')))
              and (:status = '' or b.status = :status)
              and s.campusId in :campusIds
            order by case when b.status = '待上课' then 0 when b.status = '排队中' then 1 when b.status = '已完成' then 2 else 3 end, b.id desc
            """)
    Page<Booking> searchInCampuses(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("campusIds") List<String> campusIds,
                                   Pageable pageable);

    @Query("""
            select b from Booking b
            where b.status = '待上课'
              and b.tab = 'group'
              and (b.remindSent is null or b.remindSent = false)
            """)
    List<Booking> findGroupPendingReminders();

    @Query("""
            select distinct b.userId, s.campusId from Booking b join Schedule s on b.scheduleId = s.id
            where b.userId is not null
              and s.campusId is not null and s.campusId <> ''
              and b.status in ('待上课', '排队中', '已完成')
            """)
    List<Object[]> findActiveUserCampusPairs();

    void deleteByUserId(Long userId);
}
