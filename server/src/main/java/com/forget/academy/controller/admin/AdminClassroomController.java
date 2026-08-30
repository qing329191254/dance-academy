package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.service.ClassroomAdminService;
import com.forget.academy.service.PracticeRoomBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminClassroomController {
    private final ClassroomAdminService classroomAdminService;
    private final PracticeRoomBookingService practiceRoomBookingService;

    @GetMapping("/classrooms")
    public ApiResponse<?> classrooms(@RequestParam String campusId) {
        return ApiResponse.ok(classroomAdminService.listClassrooms(campusId));
    }

    @PostMapping("/classrooms")
    public ApiResponse<?> createClassroom(@RequestParam String campusId, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(classroomAdminService.saveClassroom(campusId, body));
    }

    @PutMapping("/classrooms/{id}")
    public ApiResponse<?> updateClassroom(@PathVariable Long id,
                                          @RequestParam String campusId,
                                          @RequestBody Map<String, Object> body) {
        body.put("id", id);
        return ApiResponse.ok(classroomAdminService.saveClassroom(campusId, body));
    }

    @DeleteMapping("/classrooms/{id}")
    public ApiResponse<Void> deleteClassroom(@PathVariable Long id) {
        classroomAdminService.deleteClassroom(id);
        return ApiResponse.ok();
    }

    @GetMapping("/room-rentals")
    public ApiResponse<?> rentals(@RequestParam String campusId) {
        return ApiResponse.ok(classroomAdminService.listRentals(campusId));
    }

    @PostMapping("/room-rentals")
    public ApiResponse<?> createRental(@RequestParam String campusId, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(classroomAdminService.saveRental(campusId, body));
    }

    @PutMapping("/room-rentals/{id}")
    public ApiResponse<?> updateRental(@PathVariable Long id,
                                       @RequestParam String campusId,
                                       @RequestBody Map<String, Object> body) {
        body.put("id", id);
        return ApiResponse.ok(classroomAdminService.saveRental(campusId, body));
    }

    @PostMapping("/room-rentals/{id}/cancel")
    public ApiResponse<Void> cancelRental(@PathVariable Long id) {
        classroomAdminService.cancelRental(id);
        return ApiResponse.ok();
    }

    @GetMapping("/practice-room-bookings")
    public ApiResponse<?> practiceBookings(@RequestParam(required = false) String campusId,
                                           @RequestParam(defaultValue = "") String status,
                                           @RequestParam(defaultValue = "") String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(practiceRoomBookingService.adminList(campusId, status, keyword, page, size));
    }

    @PostMapping("/practice-room-bookings/{id}/approve")
    public ApiResponse<?> approve(@PathVariable Long id) {
        return ApiResponse.ok(practiceRoomBookingService.review(id, true, null));
    }

    @PostMapping("/practice-room-bookings/{id}/reject")
    public ApiResponse<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String reason = body == null || body.get("reason") == null ? "" : String.valueOf(body.get("reason"));
        return ApiResponse.ok(practiceRoomBookingService.review(id, false, reason));
    }
}
