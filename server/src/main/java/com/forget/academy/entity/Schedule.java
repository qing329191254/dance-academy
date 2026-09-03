package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "class_schedule")
public class Schedule extends BaseEntity {
    /** group / fixed / private */
    private String type;
    /** 校区，如 shizishan */
    private String campusId;
    private String name;
    private String timeText;
    private Long teacherId;
    private String teacherName;
    private String room;
    private Integer stars = 3;
    private String status;
    /** 0=周日 ... 6=周六，团课使用 */
    private Integer weekday;
    private Integer capacity = 20;
    /** 最低开课人数；默认 4。≤0 表示不因人数不足自动取消 */
    private Integer minEnrollment = 4;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
    /** 是否闭门团课 */
    private Boolean closedDoor = false;
    /** 闭门课面向分组，与 AppUser.closedClassGroup 对应 */
    private String audienceGroup;
    /** 所属板块（dance_category 顶级），空 = 不限 */
    private Long sectionId;
    /** 舞种（叶子节点），仅展示，不参与卡匹配 */
    private Long styleId;
}
