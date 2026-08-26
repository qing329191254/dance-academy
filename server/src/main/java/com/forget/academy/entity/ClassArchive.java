package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "class_archive", indexes = {
        @Index(name = "uk_class_archive", columnList = "teacherId,scheduleId,classDate", unique = true)
})
public class ClassArchive extends BaseEntity {
    private Long teacherId;
    private Long scheduleId;
    private String classDate;
    private String name;
    private String timeText;
    private String room;
    private String campusId;
    private String duration;
    private Instant teacherCheckedAt;
    private Integer bookedCount;
    private Integer checkedInCount;
    private String note;
    /** 学员反馈（后台录入） */
    @Column(length = 4000)
    private String studentFeedback;
    /** 续报率，如 85% */
    private String renewalRate;
}
