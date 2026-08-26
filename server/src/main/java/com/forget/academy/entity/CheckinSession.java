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
@Table(name = "checkin_session", indexes = {
        @Index(name = "idx_checkin_session_schedule", columnList = "scheduleId,classDate,active")
})
public class CheckinSession extends BaseEntity {
    private Long scheduleId;
    private String classDate;
    private String campusId;
    /** 用于二维码签名的密钥 */
    private String sessionToken;
    private Boolean active = true;
    private Long openedByUserId;
    private Instant closedAt;
}
