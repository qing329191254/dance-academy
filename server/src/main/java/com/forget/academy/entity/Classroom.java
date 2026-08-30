package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "classroom")
public class Classroom extends BaseEntity {
    @Column(nullable = false, length = 64)
    private String campusId;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(length = 40)
    private String shortName;
    private Boolean allowPractice = true;
    private Boolean allowRental = true;
    private Boolean enabled = true;
    private Integer sortOrder = 0;
}
