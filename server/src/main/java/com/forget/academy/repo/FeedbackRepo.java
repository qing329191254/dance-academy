package com.forget.academy.repo;

import com.forget.academy.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepo extends JpaRepository<Feedback, Long> {
    Page<Feedback> findByContentContainingOrNicknameContainingOrContactContaining(
            String content, String nickname, String contact, Pageable pageable);

    @Query("""
            select f from Feedback f
            where (:keyword = ''
                or lower(coalesce(f.content, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(f.nickname, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(f.contact, '')) like lower(concat('%', :keyword, '%')))
              and (coalesce(f.campusId, '') = '' or f.campusId in :campusIds)
            order by f.id desc
            """)
    Page<Feedback> searchInCampuses(@Param("keyword") String keyword,
                                    @Param("campusIds") List<String> campusIds,
                                    Pageable pageable);
}
