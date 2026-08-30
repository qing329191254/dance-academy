package com.forget.academy.repo;

import com.forget.academy.entity.PracticeRoomBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PracticeRoomBookingRepo extends JpaRepository<PracticeRoomBooking, Long> {
    List<PracticeRoomBooking> findByUserIdOrderByClassDateDescIdDesc(Long userId);

    List<PracticeRoomBooking> findByClassroomIdAndClassDateAndStatusIn(
            Long classroomId, String classDate, Collection<String> statuses);

    boolean existsByUserIdAndClassroomIdAndClassDateAndSlotIdAndStatusIn(
            Long userId, Long classroomId, String classDate, Long slotId, Collection<String> statuses);

    @Query("""
            select b from PracticeRoomBooking b
            where b.campusId in :campusIds
              and (:status = '' or b.status = :status)
              and (:keyword = ''
                or b.name like concat('%', :keyword, '%'))
            """)
    Page<PracticeRoomBooking> search(@Param("campusIds") List<String> campusIds,
                                     @Param("status") String status,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);
}
