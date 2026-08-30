package com.forget.academy.repo;

import com.forget.academy.entity.UserMemberTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserMemberTagRepo extends JpaRepository<UserMemberTag, Long> {
    List<UserMemberTag> findByUserIdInAndCampusIdIn(Collection<Long> userIds, Collection<String> campusIds);

    List<UserMemberTag> findByUserIdAndCampusId(Long userId, String campusId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserMemberTag t where t.userId = :userId and t.campusId = :campusId")
    void deleteByUserIdAndCampusId(@Param("userId") Long userId, @Param("campusId") String campusId);
}
