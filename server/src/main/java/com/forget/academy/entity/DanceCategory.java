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
@Table(name = "dance_category", indexes = {
        @Index(name = "idx_dance_category_parent", columnList = "parentId"),
        @Index(name = "uk_dance_category_code", columnList = "code", unique = true)
})
public class DanceCategory extends BaseEntity {
    /** 空 = 板块；有值 = 舞种，指向板块 id */
    private Long parentId;
    @Column(nullable = false, length = 64)
    private String name;
    /** 稳定标识，如 street / yoga */
    @Column(nullable = false, length = 64)
    private String code;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
