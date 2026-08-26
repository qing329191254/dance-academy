package com.forget.academy.repo;

import com.forget.academy.entity.EmployeeWeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeWeeklyReportRepo extends JpaRepository<EmployeeWeeklyReport, Long> {
    List<EmployeeWeeklyReport> findByUserIdOrderBySubmittedAtDesc(Long userId);
}
