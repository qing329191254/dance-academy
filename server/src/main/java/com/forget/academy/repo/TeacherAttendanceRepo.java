package com.forget.academy.repo;

import com.forget.academy.entity.TeacherAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherAttendanceRepo extends JpaRepository<TeacherAttendance, Long> {
    boolean existsByUserIdAndScheduleIdAndClassDate(Long userId, Long scheduleId, String classDate);

    List<TeacherAttendance> findByUserIdOrderByCheckedAtDesc(Long userId);

    List<TeacherAttendance> findByTeacherId(Long teacherId);

    List<TeacherAttendance> findByTeacherIdAndClassDateStartingWith(Long teacherId, String monthPrefix);

    long countByTeacherIdAndClassDateStartingWith(Long teacherId, String monthPrefix);

    long countByTeacherId(Long teacherId);

    @Query("""
            select t from TeacherAttendance t
            where (:keyword = ''
                or lower(coalesce(t.className, '')) like lower(concat('%', :keyword, '%')))
              and t.campusId in :campusIds
            order by t.id desc
            """)
    Page<TeacherAttendance> searchInCampuses(@Param("keyword") String keyword,
                                             @Param("campusIds") List<String> campusIds,
                                             Pageable pageable);
}
