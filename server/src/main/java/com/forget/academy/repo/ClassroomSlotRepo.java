package com.forget.academy.repo;

import com.forget.academy.entity.ClassroomSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassroomSlotRepo extends JpaRepository<ClassroomSlot, Long> {
    List<ClassroomSlot> findByClassroomIdOrderBySortOrderAscIdAsc(Long classroomId);

    void deleteByClassroomId(Long classroomId);
}
