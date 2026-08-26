package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "employee_duty_record", indexes = {
        @Index(name = "uk_employee_duty", columnList = "userId,scheduleId,classDate", unique = true)
})
public class EmployeeDutyRecord extends BaseEntity {
    private Long userId;
    private Long scheduleId;
    private String classDate;
    private String className;
    private String timeText;
    private String campusId;
    /** on_time / late */
    private String status;
    private Integer lateMinutes;
    private Instant checkedAt;
}
