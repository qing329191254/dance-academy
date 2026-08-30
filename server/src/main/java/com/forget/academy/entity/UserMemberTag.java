package com.forget.academy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "user_member_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_campus_tag",
                columnNames = {"userId", "campusId", "tag"}),
        indexes = {
                @Index(name = "idx_member_tag_campus_tag", columnList = "campusId,tag"),
                @Index(name = "idx_member_tag_user", columnList = "userId")
        })
public class UserMemberTag extends BaseEntity {
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String campusId;

    /** MemberTags key */
    @Column(nullable = false, length = 40)
    private String tag;
}
