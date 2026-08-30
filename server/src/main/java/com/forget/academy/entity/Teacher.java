package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "teacher")
public class Teacher extends BaseEntity {
    private String name;
    private String style;
    @Column(length = 2000)
    private String intro;
    /** 老师自填简历文字介绍 */
    @Column(length = 4000)
    private String resumeIntro;
    private String avatar;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
