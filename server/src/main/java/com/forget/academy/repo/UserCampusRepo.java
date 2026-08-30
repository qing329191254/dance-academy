package com.forget.academy.repo;

import com.forget.academy.entity.UserCampus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserCampusRepo extends JpaRepository<UserCampus, Long> {
    List<UserCampus> findByUserId(Long userId);

    List<UserCampus> findByUserIdIn(Collection<Long> userIds);

    List<UserCampus> findByUserIdInAndCampusIdIn(Collection<Long> userIds, Collection<String> campusIds);

    boolean existsByUserIdAndCampusId(Long userId, String campusId);

    boolean existsByUserIdAndCampusIdIn(Long userId, Collection<String> campusIds);

    long countByCampusId(String campusId);

    void deleteByUserIdAndCampusId(Long userId, String campusId);

    void deleteByUserIdAndCampusIdIn(Long userId, Collection<String> campusIds);
}
