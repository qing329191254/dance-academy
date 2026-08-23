package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "opportunity")
public class Opportunity extends BaseEntity {
    private String trackKey;
    @Column(length = 32)
    private String code;
    private String title;
    private LocalDate deadline;
    private Integer spots;
    private String level;
    @Column(length = 2000)
    private String summary;
    private Boolean enabled = true;
}
