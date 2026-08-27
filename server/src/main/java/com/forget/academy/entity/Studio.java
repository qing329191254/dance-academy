package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "studio")
public class Studio extends BaseEntity {
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
    @Column(length = 2000)
    private String intro;
    private String business;
    private String slogan;
    /** 课程体系列表页引导文案 */
    @Column(length = 500)
    private String courseSystemLead;
    /** 首页/列表页课程体系卡片摘要 */
    @Column(length = 500)
    private String courseSystemHomeSummary;
}
