package com.forget.academy.repo;

import com.forget.academy.entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassroomRepo extends JpaRepository<Classroom, Long> {
    List<Classroom> findByCampusIdOrderBySortOrderAscIdAsc(String campusId);

    List<Classroom> findByCampusIdAndEnabledTrueAndAllowPracticeTrueOrderBySortOrderAscIdAsc(String campusId);
}
