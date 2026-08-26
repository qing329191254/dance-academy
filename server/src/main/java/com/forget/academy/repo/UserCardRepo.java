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
}
