package com.forget.academy.repo;

import com.forget.academy.entity.CheckinSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CheckinSessionRepo extends JpaRepository<CheckinSession, Long> {
    Optional<CheckinSession> findFirstByScheduleIdAndClassDateAndActiveTrueOrderByIdDesc(
            Long scheduleId, String classDate);

    List<CheckinSession> findByScheduleIdAndClassDateAndActiveTrue(Long scheduleId, String classDate);
}
