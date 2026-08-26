package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "employee_weekly_report", indexes = @Index(name = "idx_employee_report_user", columnList = "userId"))
public class EmployeeWeeklyReport extends BaseEntity {
    private Long userId;
    /** 如 2026-W34 或 2026-08-18~2026-08-24 */
    private String weekLabel;
    @Column(length = 4000)
    private String content;
    private Instant submittedAt;
}
