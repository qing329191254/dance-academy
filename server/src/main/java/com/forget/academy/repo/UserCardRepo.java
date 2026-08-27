package com.forget.academy.repo;

import com.forget.academy.entity.UserCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserCardRepo extends JpaRepository<UserCard, Long> {
    List<UserCard> findByUserIdOrderByIdDesc(Long userId);

    List<UserCard> findByUserIdIn(Collection<Long> userIds);

    void deleteByUserId(Long userId);

    @Query("""
            select c from UserCard c
            where (:keyword = ''
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.type, '')) like lower(concat('%', :keyword, '%'))
                or concat(c.userId, '') like concat('%', :keyword, '%')
                or exists (
                    select 1 from AppUser u
                    where u.id = c.userId
                      and (lower(coalesce(u.nickname, '')) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(u.openid, '')) like lower(concat('%', :keyword, '%')))
                ))
              and (:userId is null or c.userId = :userId)
              and (:type = '' or c.type = :type)
            """)
    Page<UserCard> search(@Param("keyword") String keyword,
                          @Param("userId") Long userId,
                          @Param("type") String type,
                          Pageable pageable);

    @Query("""
            select c from UserCard c
            where (:keyword = ''
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.type, '')) like lower(concat('%', :keyword, '%'))
                or concat(c.userId, '') like concat('%', :keyword, '%')
                or exists (
                    select 1 from AppUser u
                    where u.id = c.userId
                      and (lower(coalesce(u.nickname, '')) like lower(concat('%', :keyword, '%'))
                        or lower(coalesce(u.openid, '')) like lower(concat('%', :keyword, '%')))
                ))
              and (:userId is null or c.userId = :userId)
              and (:type = '' or c.type = :type)
              and c.userId in (
                select u.id from AppUser u
                where (lower(coalesce(u.role, '')) = 'employee' and u.campusId in :campusIds)
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
    Page<UserCard> searchInCampuses(@Param("keyword") String keyword,
                                    @Param("userId") Long userId,
                                    @Param("type") String type,
                                    @Param("campusIds") List<String> campusIds,
                                    Pageable pageable);
}
