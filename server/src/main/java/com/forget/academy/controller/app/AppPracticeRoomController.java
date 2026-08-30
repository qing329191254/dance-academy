package com.forget.academy.controller.app;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.security.AuthContext;
import com.forget.academy.service.PracticeRoomBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppPracticeRoomController {
    private final PracticeRoomBookingService practiceRoomBookingService;

    @GetMapping("/practice-rooms")
    public ApiResponse<?> classrooms(@RequestParam(required = false) String campusId) {
        return ApiResponse.ok(practiceRoomBookingService.listPracticeClassrooms(campusId));
    }

    @GetMapping("/practice-rooms/{id}/slots")
    public ApiResponse<?> slots(@PathVariable Long id, @RequestParam String date) {
        return ApiResponse.ok(practiceRoomBookingService.availability(id, date));
    }

    @GetMapping("/practice-room-bookings")
    public ApiResponse<?> mine() {
        return ApiResponse.ok(practiceRoomBookingService.myBookings(AuthContext.requireApp().id()));
    }

    @PostMapping("/practice-room-bookings")
    public ApiResponse<?> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(practiceRoomBookingService.create(AuthContext.requireApp().id(), body));
    }

    @PostMapping("/practice-room-bookings/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        practiceRoomBookingService.cancel(AuthContext.requireApp().id(), id);
        return ApiResponse.ok();
    }
}
