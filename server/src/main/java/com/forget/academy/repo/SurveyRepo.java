package com.forget.academy.repo;

import com.forget.academy.entity.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyRepo extends JpaRepository<Survey, Long> {
    List<Survey> findByCampusIdOrderBySortOrderAscIdDesc(String campusId);

    List<Survey> findByCampusIdAndEnabledTrueOrderBySortOrderAscIdDesc(String campusId);
}
