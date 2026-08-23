package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_course")
public class UserCourse extends BaseEntity {
    private Long userId;
    private Long courseId;
    private String name;
    private String teacherName;
    private String progress;
    private String status;
}
