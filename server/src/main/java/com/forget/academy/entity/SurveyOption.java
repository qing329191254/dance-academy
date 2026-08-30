package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_option")
public class SurveyOption extends BaseEntity {
    private Long questionId;
    @Column(length = 300)
    private String label;
    private Integer sortOrder = 0;
}
