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
    /** 适用板块（dance_category 顶级），空 = 不限板块 */
    private Long sectionId;
    /**
     * 有效期模式：
     * from_activation = 首次到课起算有效天数；
     * fixed_deadline = 固定截止日期，逾期未开卡也作废。
     */
    private String expireMode;
    /** 有效天数（from_activation），空 = 开卡后不过期 */
    private Integer validDays;
    /** 首次到课开卡日期，空 = 未开卡 */
    private LocalDate activatedAt;
    /** 到期日：fixed_deadline 发卡时填写；from_activation 开卡后写入 */
    private LocalDate expireDate;
    private String cover;

    @Transient
    private String nickname;
    @Transient
    private String openid;
    @Transient
    private String sectionName;
}
