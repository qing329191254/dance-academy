package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "growth_track")
public class GrowthTrack extends BaseEntity {
    /** 所属校区 */
    private String campusId;
    /** parttime / intern / manage / show / commercial / teacher */
    private String trackKey;
    /** work / dance */
    private String lineKey;
    /** 勤工俭学 / 舞蹈发展 */
    private String lineName;
    /** 兼职 / 演出 等 */
    private String name;
    private String level;
    @Column(length = 1000)
    private String description;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
