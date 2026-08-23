package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user_card")
public class UserCard extends BaseEntity {
    private Long userId;
    private String name;
    private String type;
    private Integer remain;
    private Integer total;
    private LocalDate expireDate;
    private String cover;

    @Transient
    private String nickname;
    @Transient
    private String openid;
}
