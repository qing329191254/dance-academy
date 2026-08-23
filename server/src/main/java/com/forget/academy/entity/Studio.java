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
}
