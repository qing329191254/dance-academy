package com.forget.academy.repo;

import com.forget.academy.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRepo extends JpaRepository<Studio, Long> {
}
