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
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
