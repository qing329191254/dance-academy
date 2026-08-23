package com.forget.academy.repo;

import com.forget.academy.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepo extends JpaRepository<Opportunity, Long> {
    List<Opportunity> findByEnabledTrueOrderByIdAsc();

    List<Opportunity> findByTrackKeyAndEnabledTrueOrderByIdAsc(String trackKey);

    Optional<Opportunity> findByCode(String code);

    List<Opportunity> findAllByOrderByIdDesc();

    @Query("""
            select o from Opportunity o
            where (:keyword = ''
                or lower(o.title) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(o.code, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(o.summary, '')) like lower(concat('%', :keyword, '%')))
              and (:trackKey = '' or o.trackKey = :trackKey)
              and (:enabled is null or o.enabled = :enabled)
            """)
    Page<Opportunity> search(@Param("keyword") String keyword,
                             @Param("trackKey") String trackKey,
                             @Param("enabled") Boolean enabled,
                             Pageable pageable);
}
