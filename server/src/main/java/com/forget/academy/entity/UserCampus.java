package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "user_campus",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_campus",
                columnNames = {"userId", "campusId"}),
        indexes = {
                @Index(name = "idx_user_campus_campus", columnList = "campusId"),
                @Index(name = "idx_user_campus_user", columnList = "userId")
        })
public class UserCampus extends BaseEntity {
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String campusId;
}
