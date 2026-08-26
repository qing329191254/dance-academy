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
@Table(name = "teacher_attendance", indexes = {
        @Index(name = "uk_teacher_attendance", columnList = "userId,scheduleId,classDate", unique = true)
})
public class TeacherAttendance extends BaseEntity {
    private Long userId;
    private Long teacherId;
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
