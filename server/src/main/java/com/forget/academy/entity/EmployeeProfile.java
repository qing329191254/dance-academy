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
@Table(name = "employee_profile", indexes = @Index(name = "uk_employee_profile_user", columnList = "userId", unique = true))
public class EmployeeProfile extends BaseEntity {
    private Long userId;
    private String campusId;
    private String jobTitle;
    @Column(length = 2000)
    private String jobDescription;
}
