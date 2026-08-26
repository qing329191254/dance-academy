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
@Table(name = "booking", indexes = {
        @Index(name = "uk_booking_active", columnList = "userId,bookingKey", unique = true),
        @Index(name = "idx_booking_date", columnList = "classDate")
})
public class Booking extends BaseEntity {
    private Long userId;
    private String nickname;
    private Long scheduleId;
    @Column(length = 128)
    private String bookingKey;
    private String tab;
    private String classDate;
    private String name;
    private String timeText;
    private String teacherName;
    private String room;
    /** 待上课 / 排队中 / 已完成 / 已取消 */
    private String status;
    /** 团课开课前提醒是否已发送（或已跳过） */
    private Boolean remindSent = false;
    /** 预约扣减的团课卡 ID，取消待上课时返还 */
    private Long cardId;
}
