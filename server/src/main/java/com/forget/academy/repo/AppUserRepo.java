package com.forget.academy.repo;

import com.forget.academy.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppUserRepo extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByOpenid(String openid);

    Page<AppUser> findByNicknameContainingOrOpenidContaining(String nickname, String openid, Pageable pageable);

    long countBySchool(String school);

    Optional<AppUser> findFirstByTeacherId(Long teacherId);

    List<AppUser> findByTeacherIdIn(Collection<Long> teacherIds);

    @Query("""
            select u from AppUser u
            where (:keyword = ''
                or u.nickname like concat('%', :keyword, '%')
                or u.openid like concat('%', :keyword, '%'))
              and (u.role is null or u.role = '' or lower(u.role) = 'student')
            """)
    Page<AppUser> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select u from AppUser u
            where (:keyword = ''
                or u.nickname like concat('%', :keyword, '%')
                or u.openid like concat('%', :keyword, '%'))
              and lower(u.role) = lower(:role)
            """)
    Page<AppUser> searchByRole(@Param("keyword") String keyword,
                               @Param("role") String role,
                               Pageable pageable);

    @Query("""
            select u from AppUser u
            where (:keyword = ''
                or u.nickname like concat('%', :keyword, '%')
                or u.openid like concat('%', :keyword, '%'))
            """)
    Page<AppUser> searchAll(@Param("keyword") String keyword, Pageable pageable);
}
