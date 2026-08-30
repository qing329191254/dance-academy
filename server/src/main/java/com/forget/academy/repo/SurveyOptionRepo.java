package com.forget.academy.repo;

import com.forget.academy.entity.SurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyOptionRepo extends JpaRepository<SurveyOption, Long> {
    List<SurveyOption> findByQuestionIdOrderBySortOrderAscIdAsc(Long questionId);

    void deleteByQuestionId(Long questionId);
}
