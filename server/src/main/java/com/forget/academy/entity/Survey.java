package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey")
public class Survey extends BaseEntity {
    private String campusId;
    private String title;
    @Column(length = 1000)
    private String description;
    private Boolean enabled = true;
    private Integer sortOrder = 0;
}
