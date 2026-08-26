package com.forget.academy.repo;

import com.forget.academy.entity.EmployeePerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeePerformanceRepo extends JpaRepository<EmployeePerformance, Long> {
    List<EmployeePerformance> findByUserIdOrderByPublishedAtDesc(Long userId);
}
