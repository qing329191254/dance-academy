package com.forget.academy.repo;

import com.forget.academy.entity.PracticeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface PracticeRecordRepo extends JpaRepository<PracticeRecord, Long> {
    List<PracticeRecord> findByUserIdOrderByCheckedAtDesc(Long userId);

    boolean existsByUserIdAndSessionIdAndClassDate(Long userId, String sessionId, String classDate);

    long countByCheckedAtAfter(Instant after);

    Page<PracticeRecord> findByNameContaining(String name, Pageable pageable);

    void deleteByUserId(Long userId);
}
