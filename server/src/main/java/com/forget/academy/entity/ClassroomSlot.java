package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "classroom_slot")
public class ClassroomSlot extends BaseEntity {
    @Column(nullable = false)
    private Long classroomId;
    @Column(nullable = false, length = 8)
    private String startTime;
    @Column(nullable = false, length = 8)
    private String endTime;
    private Integer sortOrder = 0;
    private Boolean enabled = true;
}
