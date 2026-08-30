package com.forget.academy.repo;

import com.forget.academy.entity.SurveyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepo extends JpaRepository<SurveyResponse, Long> {
    Optional<SurveyResponse> findBySurveyIdAndUserId(Long surveyId, Long userId);

    boolean existsBySurveyIdAndUserId(Long surveyId, Long userId);

    List<SurveyResponse> findBySurveyId(Long surveyId);

    List<SurveyResponse> findByUserIdOrderByIdDesc(Long userId);

    long countBySurveyId(Long surveyId);

    @Query("""
            select r from SurveyResponse r
            where r.surveyId = :surveyId
              and r.campusId in :campusIds
              and (:keyword = ''
                or lower(coalesce(r.nickname, '')) like lower(concat('%', :keyword, '%')))
            """)
    Page<SurveyResponse> search(@Param("surveyId") Long surveyId,
                                @Param("campusIds") List<String> campusIds,
                                @Param("keyword") String keyword,
                                Pageable pageable);
}
