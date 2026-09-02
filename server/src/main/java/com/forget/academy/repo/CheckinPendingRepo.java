package com.forget.academy.repo;

import com.forget.academy.entity.CheckinPending;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CheckinPendingRepo extends JpaRepository<CheckinPending, Long> {
    Optional<CheckinPending> findByUserIdAndScheduleIdAndClassDate(Long userId, Long scheduleId, String classDate);

    Optional<CheckinPending> findByUserIdAndScheduleIdAndClassDateAndCheckinType(
            Long userId, Long scheduleId, String classDate, String checkinType);

    List<CheckinPending> findByScheduleIdAndClassDateAndStatusOrderByScannedAtAsc(
            Long scheduleId, String classDate, String status);

    @Query("""
            select p from CheckinPending p
            where p.status = :status
              and (:classDate = '' or p.classDate = :classDate)
              and (:scheduleId is null or p.scheduleId = :scheduleId)
              and p.campusId in :campusIds
              and (:keyword = ''
                or lower(coalesce(p.nickname, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(p.className, '')) like lower(concat('%', :keyword, '%')))
            order by p.scannedAt desc
            """)
    Page<CheckinPending> search(@Param("status") String status,
                                @Param("classDate") String classDate,
                                @Param("scheduleId") Long scheduleId,
                                @Param("campusIds") List<String> campusIds,
                                @Param("keyword") String keyword,
                                Pageable pageable);
}
