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

    long countByScheduleIdAndClassDateAndStatusIn(Long scheduleId, String classDate, Collection<String> statuses);

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
              and (:cancelSource = '' or coalesce(b.cancelSource, '') = :cancelSource)
              and (:scheduleId is null or b.scheduleId = :scheduleId)
              and (:classDate = '' or b.classDate = :classDate)
              and s.campusId in :campusIds
            order by case when b.status = '待上课' then 0 when b.status = '排队中' then 1 when b.status = '已完成' then 2 else 3 end, b.id desc
            """)
    Page<Booking> searchInCampuses(@Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("cancelSource") String cancelSource,
                                   @Param("scheduleId") Long scheduleId,
                                   @Param("classDate") String classDate,
                                   @Param("campusIds") List<String> campusIds,
                                   Pageable pageable);

    @Query("""
            select b from Booking b join Schedule s on b.scheduleId = s.id
            where b.status in :statuses
              and b.classDate >= :fromDate
              and (:toDate = '' or b.classDate <= :toDate)
              and s.campusId in :campusIds
            order by b.classDate asc, b.timeText asc, b.id asc
            """)
    List<Booking> findUpcomingInCampuses(@Param("statuses") Collection<String> statuses,
                                         @Param("fromDate") String fromDate,
                                         @Param("toDate") String toDate,
                                         @Param("campusIds") List<String> campusIds);

    List<Booking> findByUserIdOrderByClassDateDescIdDesc(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    @Query("""
            select min(b.classDate) from Booking b
            where b.userId = :userId
              and b.status = '已完成'
              and b.classDate is not null and b.classDate <> '' and b.classDate <> 'default'
            """)
    String findFirstCompletedClassDate(@Param("userId") Long userId);

    @Query("""
            select b from Booking b
            where b.userId = :userId
              and b.status = '已完成'
              and (:monthPrefix = '' or b.classDate like concat(:monthPrefix, '%'))
            order by b.classDate desc, b.id desc
            """)
    Page<Booking> findCompletedHistory(@Param("userId") Long userId,
                                       @Param("monthPrefix") String monthPrefix,
                                       Pageable pageable);

    @Query("""
            select b from Booking b
            where b.status = '待上课'
              and b.tab = 'group'
              and (b.remindSent is null or b.remindSent = false)
            """)
    List<Booking> findGroupPendingReminders();

    @Query("""
            select b from Booking b
            where b.status = '待上课'
              and b.tab = 'group'
              and b.classDate is not null
              and b.classDate <> ''
              and b.classDate <> 'default'
              and (b.cardConsumed is null or b.cardConsumed = false)
            """)
    List<Booking> findGroupPendingForNoShowSettle();

    @Query("""
            select distinct b.userId, s.campusId from Booking b join Schedule s on b.scheduleId = s.id
            where b.userId is not null
              and s.campusId is not null and s.campusId <> ''
              and b.status in ('待上课', '排队中', '已完成')
            """)
    List<Object[]> findActiveUserCampusPairs();

    void deleteByUserId(Long userId);
}
