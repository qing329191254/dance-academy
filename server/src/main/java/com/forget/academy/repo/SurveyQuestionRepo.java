package com.forget.academy.repo;

import com.forget.academy.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyQuestionRepo extends JpaRepository<SurveyQuestion, Long> {
    List<SurveyQuestion> findBySurveyIdOrderBySortOrderAscIdAsc(Long surveyId);

    void deleteBySurveyId(Long surveyId);
}
