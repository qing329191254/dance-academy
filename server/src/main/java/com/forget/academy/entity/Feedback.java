package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "feedback")
public class Feedback extends BaseEntity {
    private Long userId;
    private String nickname;
    private String campusId;
    private String contact;
    @Column(length = 2000)
    private String content;
}
