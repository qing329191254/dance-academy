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
@Table(name = "app_user", indexes = @Index(name = "uk_openid", columnList = "openid", unique = true))
public class AppUser extends BaseEntity {
    @Column(nullable = false, length = 64)
    private String openid;
    private String unionid;
    private String nickname;
    @Column(length = 2048)
    private String avatar;
    private String gender;
    private String birthday;
    private Boolean profileComplete = false;
    private String workLevel = "T1";
    private String workStage = "兼职";
    private String danceLevel = "T1";
    private String danceStage = "演出";
}
