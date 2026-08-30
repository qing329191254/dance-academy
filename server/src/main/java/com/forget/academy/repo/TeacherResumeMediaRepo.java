package com.forget.academy.repo;

import com.forget.academy.entity.TeacherResumeMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TeacherResumeMediaRepo extends JpaRepository<TeacherResumeMedia, Long> {
    List<TeacherResumeMedia> findByTeacherIdOrderBySortOrderAscIdAsc(Long teacherId);

    List<TeacherResumeMedia> findByTeacherIdInOrderBySortOrderAscIdAsc(Collection<Long> teacherIds);

    void deleteByTeacherId(Long teacherId);

    long countByTeacherIdAndMediaType(Long teacherId, String mediaType);
}
