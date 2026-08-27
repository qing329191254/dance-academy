package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "brand_photo")
public class BrandPhoto extends BaseEntity {
    /** 所属校区 */
    private String campusId;
    private String imageUrl;
    private Integer sortOrder = 0;
}
