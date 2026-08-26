package com.forget.academy.repo;

import com.forget.academy.entity.TeacherReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeacherReviewRepo extends JpaRepository<TeacherReview, Long> {
    Page<TeacherReview> findByTeacherIdOrderByIdDesc(Long teacherId, Pageable pageable);

    long countByTeacherId(Long teacherId);

    @Query("""
            select r from TeacherReview r
            where (:teacherId is null or r.teacherId = :teacherId)
              and (:keyword = ''
                or lower(coalesce(r.nickname, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(r.content, '')) like lower(concat('%', :keyword, '%')))
            """)
    Page<TeacherReview> search(@Param("teacherId") Long teacherId,
                               @Param("keyword") String keyword,
                               Pageable pageable);
}
