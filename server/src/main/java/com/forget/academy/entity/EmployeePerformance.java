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
@Table(name = "employee_performance", indexes = @Index(name = "idx_employee_perf_user", columnList = "userId"))
public class EmployeePerformance extends BaseEntity {
    private Long userId;
    private String periodLabel;
    @Column(length = 4000)
    private String content;
    private Instant publishedAt;
}
