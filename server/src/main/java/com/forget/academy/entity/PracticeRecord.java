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
@Table(name = "practice_record", indexes = @Index(
        name = "uk_practice",
        columnList = "userId,sessionId,classDate",
        unique = true))
public class PracticeRecord extends BaseEntity {
    private Long userId;
    private String sessionId;
    private String name;
    private String classDate;
    private String timeText;
    private String duration;
    private String teacherName;
    private String room;
    /** 签到校区 */
    private String campusId;
    /** scan=学员扫码 manual=员工/管理手动确认 */
    private String checkinSource = "scan";
    /** 手动确认人（老师姓名或管理员姓名） */
    private String operatorName;
    private Instant checkedAt;
}
