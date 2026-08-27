package com.forget.academy.repo;

import com.forget.academy.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudioRepo extends JpaRepository<Studio, Long> {
    Optional<Studio> findByCampusId(String campusId);
}
