package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "class_session_cancel", indexes = {
        @Index(name = "uk_session_cancel", columnList = "scheduleId,classDate", unique = true)
})
public class ClassSessionCancel extends BaseEntity {
    private Long scheduleId;
    @Column(length = 32)
    private String classDate;
    /** low_enrollment 等 */
    @Column(length = 64)
    private String reason;
    private Integer bookedCount;
    private Integer minEnrollment;
    private LocalDateTime cancelledAt;
}
