package com.forget.academy.repo;

import com.forget.academy.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyAnswerRepo extends JpaRepository<SurveyAnswer, Long> {
    List<SurveyAnswer> findByResponseIdOrderByIdAsc(Long responseId);

    void deleteByResponseId(Long responseId);
}
