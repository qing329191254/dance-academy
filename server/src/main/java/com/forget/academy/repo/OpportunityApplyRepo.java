package com.forget.academy.repo;

import com.forget.academy.entity.OpportunityApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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

    void deleteByUserId(Long userId);
}
