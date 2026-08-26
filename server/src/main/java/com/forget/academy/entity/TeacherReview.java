package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teacher_review", indexes = {
        @Index(name = "idx_teacher_review_teacher", columnList = "teacherId")
})
public class TeacherReview extends BaseEntity {
    private Long teacherId;
    private Long userId;
    private String nickname;
    @Column(length = 2000)
    private String content;
}
