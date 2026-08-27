package com.forget.academy.repo;

import com.forget.academy.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepo extends JpaRepository<Course, Long> {
    List<Course> findByEnabledTrueOrderBySortOrderAscIdAsc();

    List<Course> findByModuleTypeAndEnabledTrueOrderBySortOrderAscIdAsc(String moduleType);

    long countByModuleType(String moduleType);

    List<Course> findAllByOrderBySortOrderAscIdAsc();

    @Query("""
            select c from Course c
            where (:keyword = ''
                or lower(c.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.level, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(c.description, '')) like lower(concat('%', :keyword, '%')))
              and (:enabled is null or c.enabled = :enabled)
              and (:moduleType = '' or coalesce(c.moduleType, 'product') = :moduleType)
            """)
    Page<Course> search(@Param("keyword") String keyword,
                        @Param("enabled") Boolean enabled,
                        @Param("moduleType") String moduleType,
                        Pageable pageable);
}
