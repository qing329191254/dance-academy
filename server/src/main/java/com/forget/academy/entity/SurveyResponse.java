package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_response")
public class SurveyResponse extends BaseEntity {
    private Long surveyId;
    private Long userId;
    private String nickname;
    private String campusId;
}
