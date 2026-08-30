package com.forget.academy.repo;

import com.forget.academy.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTypeAndEnabledTrueOrderBySortOrderAscIdAsc(String type);

    List<Schedule> findByTypeAndCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(String type, String campusId);

    List<Schedule> findByTypeAndWeekdayAndEnabledTrueOrderBySortOrderAscIdAsc(String type, Integer weekday);

    List<Schedule> findByTypeAndWeekdayAndCampusIdAndEnabledTrueOrderBySortOrderAscIdAsc(
            String type, Integer weekday, String campusId);

    List<Schedule> findByTeacherIdAndEnabledTrueOrderBySortOrderAscIdAsc(Long teacherId);

    List<Schedule> findByCampusIdAndEnabledTrue(String campusId);

    List<Schedule> findAllByOrderByTypeAscSortOrderAscIdAsc();

    long countByCampusId(String campusId);

    @Query("""
            select s from Schedule s
            where (:keyword = ''
                or lower(s.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(s.teacherName, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(s.room, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(s.timeText, '')) like lower(concat('%', :keyword, '%')))
              and (:type = '' or s.type = :type)
              and s.campusId in :campusIds
              and (:enabled is null or s.enabled = :enabled)
            """)
    Page<Schedule> search(@Param("keyword") String keyword,
                          @Param("type") String type,
                          @Param("campusIds") List<String> campusIds,
                          @Param("enabled") Boolean enabled,
                          Pageable pageable);
}
