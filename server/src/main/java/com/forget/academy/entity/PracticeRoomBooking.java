package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "practice_room_booking")
public class PracticeRoomBooking extends BaseEntity {
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, length = 64)
    private String campusId;
    @Column(nullable = false)
    private Long classroomId;
    @Column(nullable = false)
    private Long slotId;
    @Column(nullable = false, length = 10)
    private String classDate;
    @Column(nullable = false, length = 8)
    private String startTime;
    @Column(nullable = false, length = 8)
    private String endTime;
    @Column(nullable = false, length = 40)
    private String name;
    /** pending / approved / rejected / cancelled */
    @Column(length = 20)
    private String status = "pending";
    @Column(length = 200)
    private String rejectReason;
}
