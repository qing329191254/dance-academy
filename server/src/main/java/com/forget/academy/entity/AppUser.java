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
    private String phone;
    private String school;
    private String collegeGrade;
    /** student / teacher / employee */
    private String role = "student";
    /** 老师角色绑定的 Teacher.id */
    private Long teacherId;
    /** 员工角色绑定的校区 */
    private String campusId;
    private Boolean profileComplete = false;
    private String workLevel = "T1";
    private String workStage = "兼职";
    private String danceLevel = "T1";
    private String danceStage = "演出";
    /** 闭门课分组：advanced=高潜闭门 / foundation=基础闭门，空=普通学员 */
    private String closedClassGroup;
}
