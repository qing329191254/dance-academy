package com.forget.academy.repo;

import com.forget.academy.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherRepo extends JpaRepository<Teacher, Long> {
    List<Teacher> findByEnabledTrueOrderBySortOrderAscIdAsc();

    List<Teacher> findAllByOrderBySortOrderAscIdAsc();

    @Query("""
            select t from Teacher t
            where (:keyword = ''
                or lower(t.name) like lower(concat('%', :keyword, '%'))
                or lower(t.style) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(t.intro, '')) like lower(concat('%', :keyword, '%')))
              and (:enabled is null or t.enabled = :enabled)
            """)
    Page<Teacher> search(@Param("keyword") String keyword,
                         @Param("enabled") Boolean enabled,
                         Pageable pageable);

    @Query("""
            select distinct t from Teacher t
            join Schedule s on s.teacherId = t.id
            where s.campusId in :campusIds
              and (:keyword = ''
                or lower(t.name) like lower(concat('%', :keyword, '%'))
                or lower(t.style) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(t.intro, '')) like lower(concat('%', :keyword, '%')))
              and (:enabled is null or t.enabled = :enabled)
            """)
    Page<Teacher> searchInCampuses(@Param("keyword") String keyword,
                                   @Param("enabled") Boolean enabled,
                                   @Param("campusIds") List<String> campusIds,
                                   Pageable pageable);
}
