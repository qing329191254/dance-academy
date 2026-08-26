package com.forget.academy.repo;

import com.forget.academy.entity.EmployeeDutyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeDutyRecordRepo extends JpaRepository<EmployeeDutyRecord, Long> {
    boolean existsByUserIdAndScheduleIdAndClassDate(Long userId, Long scheduleId, String classDate);

    List<EmployeeDutyRecord> findByUserIdOrderByCheckedAtDesc(Long userId);

    @Query("""
            select e from EmployeeDutyRecord e
            where (:keyword = ''
                or lower(coalesce(e.className, '')) like lower(concat('%', :keyword, '%')))
              and e.campusId in :campusIds
            order by e.id desc
            """)
    Page<EmployeeDutyRecord> searchInCampuses(@Param("keyword") String keyword,
                                              @Param("campusIds") List<String> campusIds,
                                              Pageable pageable);
}
