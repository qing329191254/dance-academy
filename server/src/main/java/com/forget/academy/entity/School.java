package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "school_option", indexes = @Index(name = "uk_school_name", columnList = "name", unique = true))
public class School extends BaseEntity {
    @Column(nullable = false, length = 80)
    private String name;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
