package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "room_rental")
public class RoomRental extends BaseEntity {
    @Column(nullable = false, length = 64)
    private String campusId;
    @Column(nullable = false)
    private Long classroomId;
    @Column(nullable = false, length = 10)
    private String classDate;
    @Column(nullable = false, length = 8)
    private String startTime;
    @Column(nullable = false, length = 8)
    private String endTime;
    @Column(length = 80)
    private String contactName;
    @Column(length = 40)
    private String phone;
    @Column(length = 500)
    private String remark;
    /** confirmed / cancelled */
    @Column(length = 20)
    private String status = "confirmed";
}
