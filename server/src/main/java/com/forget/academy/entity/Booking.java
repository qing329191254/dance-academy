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
    /**
     * 取消来源（仅已取消有意义）：
     * user=学员自主 / admin=后台取消 / system_low_enrollment=人数不足系统取消
     */
    @Column(length = 40)
    private String cancelSource;
    /** 团课开课前提醒是否已发送（或已跳过） */
    private Boolean remindSent = false;
    /** 预约锁定的卡 ID，到课成功后再扣次 */
    private Long cardId;
    /** 是否已在到课时扣过次 */
    private Boolean cardConsumed = false;
}
