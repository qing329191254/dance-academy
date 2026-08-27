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

    @Query("""
            select u from AppUser u
            where (:keyword = ''
                or u.nickname like concat('%', :keyword, '%')
                or u.openid like concat('%', :keyword, '%'))
              and (
                :roleFilter = ''
                or (:roleFilter = 'student' and (u.role is null or u.role = '' or lower(u.role) = 'student'))
                or lower(u.role) = lower(:roleFilter)
              )
              and (
                lower(coalesce(u.role, '')) = 'employee' and u.campusId in :campusIds
                or (lower(coalesce(u.role, '')) = 'teacher' and u.teacherId in (
                    select distinct s.teacherId from Schedule s
                    where s.campusId in :campusIds and s.teacherId is not null))
                or ((u.role is null or u.role = '' or lower(u.role) = 'student') and (
                    exists (
                        select 1 from Booking b join Schedule s on b.scheduleId = s.id
                        where b.userId = u.id and s.campusId in :campusIds)
                    or exists (
                        select 1 from PracticeRecord p
                        where p.userId = u.id and p.campusId in :campusIds)))
              )
            """)
    Page<AppUser> searchInCampuses(@Param("keyword") String keyword,
                                   @Param("roleFilter") String roleFilter,
                                   @Param("campusIds") List<String> campusIds,
                                   Pageable pageable);

    @Query("""
            select count(u) from AppUser u
            where u.role is null or u.role = '' or lower(u.role) = 'student'
            """)
    long countStudents();

    @Query("""
            select count(distinct u.id) from AppUser u
            where (u.role is null or u.role = '' or lower(u.role) = 'student')
              and (
                exists (
                    select 1 from Booking b join Schedule s on b.scheduleId = s.id
                    where b.userId = u.id and s.campusId in :campusIds
                )
                or exists (
                    select 1 from PracticeRecord p
                    where p.userId = u.id and p.campusId in :campusIds
                )
              )
            """)
    long countStudentsActiveInCampuses(@Param("campusIds") List<String> campusIds);
}
