package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course")
public class Course extends BaseEntity {
    private String name;
    private Integer price;
    /** 展示价，如 9.9；为空则用 price */
    private String priceDisplay;
    private String priceUnit = "节";
    private String level;
    @Column(length = 2000)
    private String description;
    /** 列表摘要 */
    @Column(length = 500)
    private String summary;
    /** 标签，如新人专享 */
    private String tag;
    /** trial / system / product */
    private String moduleType = "product";
    /** system 模块标识，如 fixed/pass/private/custom */
    private String moduleKey;
    @Column(length = 2000)
    private String highlights;
    private String actionLabel;
    /** 约课 tab：group/fixed/private，空则拨打电话 */
    private String actionTab;
    private String cover;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
