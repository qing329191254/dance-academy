package com.forget.academy.repo;

import com.forget.academy.entity.OpportunityApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OpportunityApplyRepo extends JpaRepository<OpportunityApply, Long> {
    Optional<OpportunityApply> findByUserIdAndOpportunityId(Long userId, Long opportunityId);

    List<OpportunityApply> findByUserIdOrderByIdDesc(Long userId);

    List<OpportunityApply> findByUserIdInOrderByIdDesc(Collection<Long> userIds);

    long countByStatus(String status);

    Page<OpportunityApply> findByStatus(String status, Pageable pageable);

    Page<OpportunityApply> findByTitleContainingOrNicknameContaining(String title, String nickname, Pageable pageable);

    @Query("""
            select count(a) from OpportunityApply a
            where a.status = :status
              and a.userId in (
                select distinct u.id from AppUser u
                where exists (
                    select 1 from Booking b join Schedule s on b.scheduleId = s.id
                    where b.userId = u.id and s.campusId in :campusIds
                )
                or exists (
                    select 1 from PracticeRecord p
                    where p.userId = u.id and p.campusId in :campusIds
                )
              )
            """)
    long countByStatusAndUserActiveInCampuses(@Param("status") String status,
                                              @Param("campusIds") List<String> campusIds);

    @Query("""
            select a from OpportunityApply a
            where a.userId in (
                select distinct u.id from AppUser u
                where exists (
                    select 1 from Booking b join Schedule s on b.scheduleId = s.id
                    where b.userId = u.id and s.campusId in :campusIds
                )
                or exists (
                    select 1 from PracticeRecord p
                    where p.userId = u.id and p.campusId in :campusIds
                )
              )
            order by a.id desc
            """)
    Page<OpportunityApply> findLatestByUserActiveInCampuses(@Param("campusIds") List<String> campusIds,
                                                            Pageable pageable);

    @Query("""
            select a from OpportunityApply a
            where (:status = '' or a.status = :status)
              and (:keyword = ''
                or lower(coalesce(a.title, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.nickname, '')) like lower(concat('%', :keyword, '%')))
              and a.userId in (
                select distinct u.id from AppUser u
                where exists (
                    select 1 from Booking b join Schedule s on b.scheduleId = s.id
                    where b.userId = u.id and s.campusId in :campusIds)
                or exists (
                    select 1 from PracticeRecord p
                    where p.userId = u.id and p.campusId in :campusIds))
            order by a.id desc
            """)
    Page<OpportunityApply> searchInCampuses(@Param("keyword") String keyword,
                                            @Param("status") String status,
                                            @Param("campusIds") List<String> campusIds,
                                            Pageable pageable);

    @Query("""
            select a from OpportunityApply a
            where (:status = '' or a.status = :status)
              and (:keyword = ''
                or lower(coalesce(a.title, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.nickname, '')) like lower(concat('%', :keyword, '%')))
            order by a.id desc
            """)
    Page<OpportunityApply> searchAll(@Param("keyword") String keyword,
                                     @Param("status") String status,
                                     Pageable pageable);

    void deleteByUserId(Long userId);
}
