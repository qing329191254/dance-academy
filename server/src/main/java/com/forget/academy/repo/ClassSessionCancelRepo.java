package com.forget.academy.repo;

import com.forget.academy.entity.ClassSessionCancel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassSessionCancelRepo extends JpaRepository<ClassSessionCancel, Long> {
    boolean existsByScheduleIdAndClassDate(Long scheduleId, String classDate);

    Optional<ClassSessionCancel> findByScheduleIdAndClassDate(Long scheduleId, String classDate);
}
