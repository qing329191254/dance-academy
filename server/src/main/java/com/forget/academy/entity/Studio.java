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
@Table(name = "studio", indexes = {
        @Index(name = "uk_studio_campus", columnList = "campusId", unique = true)
})
public class Studio extends BaseEntity {
    /** 所属校区，每校区一条门店配置 */
    private String campusId;
    private String name;
    private String location;
    private String city;
    private String address;
    private Double latitude;
    private Double longitude;
    private String businessHours;
    private String phone;
    private String phoneDisplay;
    private String logo;
    private String splashImage;
    /** 小程序全局分享标题（好友转发 / 朋友圈共用） */
    @Column(length = 64)
    private String shareTitle;
    /** 小程序全局分享封面，建议 5:4 */
    private String shareImage;
    @Column(length = 2000)
    private String intro;
    private String business;
    private String slogan;
    /** 课程产品列表页引导文案 */
    @Column(length = 500)
    private String courseSystemLead;
    /** 首页/列表页课程产品卡片摘要 */
    @Column(length = 500)
    private String courseSystemHomeSummary;
    /** 成长中心首页介绍 */
    @Column(length = 2000)
    private String growthIntro;
    /** 成长中心等级提示 */
    @Column(length = 500)
    private String growthLevelTip;
    /** 勤工俭学页引导文案 */
    @Column(length = 500)
    private String workLead;
    /** 舞蹈发展页引导文案 */
    @Column(length = 500)
    private String danceLead;
    /** 成长中心勤工俭学卡片摘要 */
    @Column(length = 500)
    private String workModuleSummary;
    /** 成长中心舞蹈发展卡片摘要 */
    @Column(length = 500)
    private String danceModuleSummary;
    /** 学员须知正文（## 标题 + 段落，按校区配置） */
    @Column(length = 8000)
    private String studentNotice;
    /** 运营主体（隐私政策/用户协议展示） */
    @Column(length = 120)
    private String company;
    /** 联系邮箱 */
    @Column(length = 120)
    private String email;
    /** 协议更新日期展示，如 2026年8月31日 */
    @Column(length = 32)
    private String legalUpdateDate;
}
