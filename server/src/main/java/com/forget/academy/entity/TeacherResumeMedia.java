package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teacher_resume_media")
public class TeacherResumeMedia extends BaseEntity {
    private Long teacherId;
    /** photo | video */
    private String mediaType;
    @Column(length = 500)
    private String url;
    private Integer sortOrder = 0;
}
