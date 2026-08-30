package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_question")
public class SurveyQuestion extends BaseEntity {
    private Long surveyId;
    /** text | single | multi */
    private String type;
    @Column(length = 500)
    private String title;
    private Boolean required = true;
    private Integer sortOrder = 0;
}
