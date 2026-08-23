package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course")
public class Course extends BaseEntity {
    private String name;
    private Integer price;
    private String level;
    @Column(length = 2000)
    private String description;
    private String cover;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
