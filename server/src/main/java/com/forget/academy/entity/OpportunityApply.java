package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "opportunity_apply", indexes = @Index(
        name = "uk_apply",
        columnList = "userId,opportunityId",
        unique = true))
public class OpportunityApply extends BaseEntity {
    private Long userId;
    private Long opportunityId;
    private String trackKey;
    private String title;
    private String nickname;
    /** pending / approved / rejected / cancelled */
    private String status;
    @jakarta.persistence.Column(length = 2048)
    private String resumeUrl;
    private String resumeName;
}
