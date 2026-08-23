package com.forget.academy.repo;

import com.forget.academy.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCourseRepo extends JpaRepository<UserCourse, Long> {
    List<UserCourse> findByUserIdOrderByIdDesc(Long userId);

    void deleteByUserId(Long userId);
}
