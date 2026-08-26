package com.forget.academy.repo;

import com.forget.academy.entity.PracticeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PracticeRecordRepo extends JpaRepository<PracticeRecord, Long> {
    List<PracticeRecord> findByUserIdOrderByCheckedAtDesc(Long userId);

    boolean existsByUserIdAndSessionIdAndClassDate(Long userId, String sessionId, String classDate);

    java.util.Optional<PracticeRecord> findByUserIdAndSessionIdAndClassDate(
            Long userId, String sessionId, String classDate);

    long countByCheckedAtAfter(Instant after);

    long countBySessionIdAndClassDate(String sessionId, String classDate);

    List<PracticeRecord> findBySessionIdAndClassDateOrderByCheckedAtAsc(String sessionId, String classDate);

    Page<PracticeRecord> findByNameContaining(String name, Pageable pageable);

    @Query("""
            select p from PracticeRecord p
            where (:keyword = '' or lower(coalesce(p.name, '')) like lower(concat('%', :keyword, '%')))
              and p.campusId in :campusIds
            order by p.id desc
            """)
    Page<PracticeRecord> searchInCampuses(@Param("keyword") String keyword,
                                          @Param("campusIds") List<String> campusIds,
                                          Pageable pageable);

    @Query("""
            select count(p) from PracticeRecord p
            where p.checkedAt >= :after and p.campusId in :campusIds
            """)
    long countByCheckedAtAfterAndCampusIdIn(@Param("after") Instant after,
                                            @Param("campusIds") List<String> campusIds);

    void deleteByUserId(Long userId);

    @Query("""
            select p.userId, count(p)
            from PracticeRecord p
            where (:campusId = '' or p.campusId = :campusId)
              and (:fromDate = '' or p.classDate >= :fromDate)
            group by p.userId
            order by count(p) desc, p.userId asc
            """)
    List<Object[]> rankByCheckin(@Param("campusId") String campusId, @Param("fromDate") String fromDate);
}
