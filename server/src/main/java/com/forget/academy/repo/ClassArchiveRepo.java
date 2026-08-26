package com.forget.academy.repo;

import com.forget.academy.entity.ClassArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassArchiveRepo extends JpaRepository<ClassArchive, Long> {
    Optional<ClassArchive> findByTeacherIdAndScheduleIdAndClassDate(Long teacherId, Long scheduleId, String classDate);

    List<ClassArchive> findByTeacherIdOrderByClassDateDescIdDesc(Long teacherId);

    Page<ClassArchive> findByTeacherIdOrderByClassDateDescIdDesc(Long teacherId, Pageable pageable);

    long countByTeacherIdAndTeacherCheckedAtIsNotNull(Long teacherId);

    long countByTeacherIdAndTeacherCheckedAtIsNotNullAndClassDateStartingWith(Long teacherId, String monthPrefix);

    @Query("""
            select a from ClassArchive a
            where (:keyword = ''
                or lower(coalesce(a.name, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.room, '')) like lower(concat('%', :keyword, '%')))
              and (:teacherId is null or a.teacherId = :teacherId)
              and a.campusId in :campusIds
            order by a.classDate desc, a.id desc
            """)
    Page<ClassArchive> search(@Param("keyword") String keyword,
                              @Param("teacherId") Long teacherId,
                              @Param("campusIds") List<String> campusIds,
                              Pageable pageable);
}
