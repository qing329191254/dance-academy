package com.forget.academy.repo;

import com.forget.academy.entity.UserMemberTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserMemberTagRepo extends JpaRepository<UserMemberTag, Long> {
    List<UserMemberTag> findByUserIdInAndCampusIdIn(Collection<Long> userIds, Collection<String> campusIds);

    List<UserMemberTag> findByUserIdAndCampusId(Long userId, String campusId);

    void deleteByUserIdAndCampusId(Long userId, String campusId);
}
