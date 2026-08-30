package com.forget.academy.repo;

import com.forget.academy.entity.RoomRental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRentalRepo extends JpaRepository<RoomRental, Long> {
    List<RoomRental> findByCampusIdAndClassDateAndStatusOrderByStartTimeAscIdAsc(
            String campusId, String classDate, String status);

    List<RoomRental> findByCampusIdOrderByClassDateDescIdDesc(String campusId);

    List<RoomRental> findByClassroomIdAndClassDateAndStatus(Long classroomId, String classDate, String status);
}
