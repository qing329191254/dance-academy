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
@Table(name = "practice_record", indexes = @Index(
        name = "uk_practice",
        columnList = "userId,sessionId,classDate",
        unique = true))
public class PracticeRecord extends BaseEntity {
    private Long userId;
    private String sessionId;
    private String name;
    private String classDate;
    private String timeText;
    private String duration;
    private String teacherName;
    private String room;
    private Instant checkedAt;
}
