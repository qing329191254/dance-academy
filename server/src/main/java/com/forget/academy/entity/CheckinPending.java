package com.forget.academy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "checkin_pending", indexes = {
        @Index(name = "uk_checkin_pending", columnList = "userId,scheduleId,classDate", unique = true),
        @Index(name = "idx_checkin_pending_status", columnList = "status,classDate,campusId")
})
public class CheckinPending extends BaseEntity {
    private Long userId;
    /** student / teacher / employee */
    private String role;
    private Long scheduleId;
    private String classDate;
    private String campusId;
    private String nickname;
    private String className;
    private String timeText;
    private String teacherName;
    private String room;
    /** pending / confirmed / rejected */
    private String status;
    private Long checkinSessionId;
    private Instant scannedAt;
    private Instant confirmedAt;
    private Long confirmedByUserId;
    private String confirmedByName;
}
