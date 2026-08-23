package com.forget.academy.controller.admin;

import com.forget.academy.common.ApiResponse;
import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.Booking;
import com.forget.academy.entity.Opportunity;
import com.forget.academy.entity.OpportunityApply;
import com.forget.academy.entity.PracticeRecord;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.OpportunityApplyRepo;
import com.forget.academy.repo.OpportunityRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminOpsController {
    private final BookingRepo bookingRepo;
    private final OpportunityRepo opportunityRepo;
    private final OpportunityApplyRepo applyRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final AppUserRepo appUserRepo;

    @GetMapping("/dashboard")
    public ApiResponse<?> dashboard() {
        String today = LocalDate.now(ZoneId.of("Asia/Shanghai")).toString();
        Instant weekAgo = Instant.now().minusSeconds(7 * 24 * 3600L);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", appUserRepo.count());
        data.put("bookingToday", bookingRepo.countByClassDateAndStatus(today, "待上课"));
        data.put("pendingApplies", applyRepo.countByStatus("pending"));
        data.put("practiceWeek", practiceRecordRepo.countByCheckedAtAfter(weekAgo));
        data.put("latestBookings", bookingRepo.findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        data.put("latestApplies", applyRepo.findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"))).getContent());
        return ApiResponse.ok(data);
    }

    @GetMapping("/bookings")
    public ApiResponse<?> bookings(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "") String status,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size);
        String query = keyword == null ? "" : keyword.trim();
        String st = status == null ? "" : status.trim();
        return ApiResponse.ok(PageResult.of(bookingRepo.search(query, st, pageable)));
    }

    @PutMapping("/bookings/{id}")
    public ApiResponse<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking body) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new BizException("预约不存在"));
        if (body.getStatus() != null) {
            booking.setStatus(body.getStatus());
        }
        return ApiResponse.ok(bookingRepo.save(booking));
    }

    @DeleteMapping("/bookings/{id}")
    public ApiResponse<Void> deleteBooking(@PathVariable Long id) {
        bookingRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/opportunities")
    public ApiResponse<?> opportunities(@RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size,
                                        @RequestParam(defaultValue = "") String keyword,
                                        @RequestParam(defaultValue = "") String trackKey,
                                        @RequestParam(required = false) Boolean enabled) {
        if (page == null) {
            return ApiResponse.ok(opportunityRepo.findAllByOrderByIdDesc());
        }
        int pageSize = size == null ? 20 : Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by(Sort.Direction.DESC, "id"));
        String query = keyword == null ? "" : keyword.trim();
        String track = trackKey == null ? "" : trackKey.trim();
        return ApiResponse.ok(PageResult.of(opportunityRepo.search(query, track, enabled, pageable)));
    }

    @PostMapping("/opportunities")
    public ApiResponse<Opportunity> createOpportunity(@RequestBody Opportunity body) {
        body.setId(null);
        if (body.getEnabled() == null) {
            body.setEnabled(true);
        }
        body.setCode(nextOpportunityCode(body.getTrackKey()));
        return ApiResponse.ok(opportunityRepo.save(body));
    }

    @PutMapping("/opportunities/{id}")
    public ApiResponse<Opportunity> updateOpportunity(@PathVariable Long id, @RequestBody Opportunity body) {
        Opportunity item = opportunityRepo.findById(id).orElseThrow(() -> new BizException("机会不存在"));
        item.setTrackKey(body.getTrackKey());
        if (item.getCode() == null || item.getCode().isBlank()) {
            item.setCode(nextOpportunityCode(body.getTrackKey()));
        }
        item.setTitle(body.getTitle());
        item.setDeadline(body.getDeadline());
        item.setSpots(body.getSpots());
        item.setLevel(body.getLevel());
        item.setSummary(body.getSummary());
        item.setEnabled(body.getEnabled());
        return ApiResponse.ok(opportunityRepo.save(item));
    }

    @DeleteMapping("/opportunities/{id}")
    public ApiResponse<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityRepo.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/applies")
    public ApiResponse<?> applies(@RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(defaultValue = "") String status,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        Page<OpportunityApply> result;
        if (status != null && !status.isBlank()) {
            result = applyRepo.findByStatus(status, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = applyRepo.findByTitleContainingOrNicknameContaining(keyword, keyword, pageable);
        } else {
            result = applyRepo.findAll(pageable);
        }
        return ApiResponse.ok(PageResult.of(result));
    }

    @PutMapping("/applies/{id}")
    public ApiResponse<OpportunityApply> updateApply(@PathVariable Long id, @RequestBody OpportunityApply body) {
        OpportunityApply apply = applyRepo.findById(id).orElseThrow(() -> new BizException("报名不存在"));
        if (body.getStatus() != null) {
            apply.setStatus(body.getStatus());
        }
        return ApiResponse.ok(applyRepo.save(apply));
    }

    @GetMapping("/practice")
    public ApiResponse<?> practice(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PracticeRecord> result = (keyword == null || keyword.isBlank())
                ? practiceRecordRepo.findAll(pageable)
                : practiceRecordRepo.findByNameContaining(keyword, pageable);
        return ApiResponse.ok(PageResult.of(result));
    }

    private String nextOpportunityCode(String trackKey) {
        String prefix = switch (trackKey == null ? "" : trackKey) {
            case "show", "commercial", "teacher" -> "d";
            default -> "w";
        };
        int seq = 1;
        while (opportunityRepo.findByCode(prefix + seq).isPresent()) {
            seq++;
        }
        return prefix + seq;
    }
}
