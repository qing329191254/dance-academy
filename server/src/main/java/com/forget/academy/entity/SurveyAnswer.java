package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_answer")
public class SurveyAnswer extends BaseEntity {
    private Long responseId;
    private Long questionId;
    @Column(length = 2000)
    private String textValue;
    /** 单选/多选选项 id，逗号分隔 */
    @Column(length = 500)
    private String optionIds;
}
