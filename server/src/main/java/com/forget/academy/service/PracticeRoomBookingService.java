package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Classroom;
import com.forget.academy.entity.ClassroomSlot;
import com.forget.academy.entity.PracticeRoomBooking;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.ClassroomRepo;
import com.forget.academy.repo.ClassroomSlotRepo;
import com.forget.academy.repo.PracticeRoomBookingRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PracticeRoomBookingService {
    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String CANCELLED = "cancelled";

    private static final Set<String> ACTIVE = Set.of(PENDING, APPROVED);

    private final ClassroomRepo classroomRepo;
    private final ClassroomSlotRepo classroomSlotRepo;
    private final PracticeRoomBookingRepo bookingRepo;
    private final AppUserRepo appUserRepo;
    private final RoomAvailabilityService roomAvailabilityService;
    private final ClassroomAdminService classroomAdminService;
    private final AdminAccessService adminAccessService;
    private final CampusCatalogService campusCatalogService;
    private final UserCampusService userCampusService;

    public List<Map<String, Object>> listPracticeClassrooms(String campusId) {
        String campus = campusCatalogService.normalize(campusId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Classroom classroom : classroomRepo.findByCampusIdAndEnabledTrueAndAllowPracticeTrueOrderBySortOrderAscIdAsc(campus)) {
            Map<String, Object> map = classroomAdminService.toClassroomMap(classroom, true);
            List<Map<String, Object>> enabledSlots = new ArrayList<>();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> slots = (List<Map<String, Object>>) map.get("slots");
            if (slots != null) {
                for (Map<String, Object> slot : slots) {
                    if (Boolean.TRUE.equals(slot.get("enabled"))) {
                        enabledSlots.add(slot);
                    }
                }
            }
            map.put("slots", enabledSlots);
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> availability(Long classroomId, String date) {
        Classroom classroom = classroomRepo.findById(classroomId).orElseThrow(() -> new BizException("教室不存在"));
        if (!Boolean.TRUE.equals(classroom.getEnabled()) || !Boolean.TRUE.equals(classroom.getAllowPractice())) {
            throw new BizException("该教室暂不可练舞预约");
        }
        validateDate(date);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClassroomSlot slot : classroomSlotRepo.findByClassroomIdOrderBySortOrderAscIdAsc(classroomId)) {
            if (!Boolean.TRUE.equals(slot.getEnabled())) {
                continue;
            }
            Map<String, Object> row = classroomAdminService.toSlotMap(slot);
            boolean occupied = roomAvailabilityService.isOccupiedByClassOrRental(
                    classroomId, date, slot.getStartTime(), slot.getEndTime());
            long count = bookingRepo.findByClassroomIdAndClassDateAndStatusIn(classroomId, date, ACTIVE).stream()
                    .filter(item -> slot.getId().equals(item.getSlotId()))
                    .count();
            row.put("occupied", occupied);
            row.put("available", !occupied);
            row.put("practiceCount", count);
            result.add(row);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> create(Long userId, Map<String, Object> body) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        Long classroomId = longVal(body.get("classroomId"));
        Long slotId = longVal(body.get("slotId"));
        String date = str(body.get("classDate"));
        String name = str(body.get("name"));
        if (name.isBlank()) {
            name = user.getNickname() == null ? "" : user.getNickname().trim();
        }
        if (name.isBlank()) {
            throw new BizException("请填写姓名");
        }
        validateDate(date);
        Classroom classroom = classroomRepo.findById(classroomId == null ? -1 : classroomId)
                .orElseThrow(() -> new BizException("请选择教室"));
        if (!Boolean.TRUE.equals(classroom.getEnabled()) || !Boolean.TRUE.equals(classroom.getAllowPractice())) {
            throw new BizException("该教室暂不可练舞预约");
        }
        ClassroomSlot slot = classroomSlotRepo.findById(slotId == null ? -1 : slotId)
                .orElseThrow(() -> new BizException("请选择时段"));
        if (!classroom.getId().equals(slot.getClassroomId()) || !Boolean.TRUE.equals(slot.getEnabled())) {
            throw new BizException("时段无效");
        }
        if (bookingRepo.existsByUserIdAndClassroomIdAndClassDateAndSlotIdAndStatusIn(
                userId, classroom.getId(), date, slot.getId(), ACTIVE)) {
            throw new BizException("该时段已预约，请等待审核或查看记录");
        }
        if (roomAvailabilityService.isOccupiedByClassOrRental(
                classroom.getId(), date, slot.getStartTime(), slot.getEndTime())) {
            throw new BizException("该时段教室已被占用");
        }
        PracticeRoomBooking booking = new PracticeRoomBooking();
        booking.setUserId(userId);
        booking.setCampusId(classroom.getCampusId());
        booking.setClassroomId(classroom.getId());
        booking.setSlotId(slot.getId());
        booking.setClassDate(date);
        booking.setStartTime(slot.getStartTime());
        booking.setEndTime(slot.getEndTime());
        booking.setName(name);
        booking.setStatus(PENDING);
        PracticeRoomBooking saved = bookingRepo.save(booking);
        userCampusService.ensureLinked(userId, classroom.getCampusId());
        return toAppMap(saved);
    }

    public PageResult<Map<String, Object>> myBookings(Long userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        var pageable = PageRequest.of(Math.max(page - 1, 0), safeSize);
        var result = bookingRepo.findByUserIdOrderByClassDateDescIdDesc(userId, pageable);
        List<Map<String, Object>> list = result.getContent().stream().map(this::toAppMap).toList();
        return new PageResult<>(list, result.getTotalElements(), result.getNumber() + 1, result.getSize());
    }

    @Transactional
    public void cancel(Long userId, Long id) {
        PracticeRoomBooking booking = bookingRepo.findById(id).orElseThrow(() -> new BizException("预约不存在"));
        if (!userId.equals(booking.getUserId())) {
            throw new BizException("无权操作");
        }
        if (!ACTIVE.contains(booking.getStatus())) {
            throw new BizException("当前状态不可取消");
        }
        booking.setStatus(CANCELLED);
        bookingRepo.save(booking);
    }

    public PageResult<Map<String, Object>> adminList(String campusId, String status, String keyword, int page, int size) {
        var campuses = adminAccessService.resolveCampusScope(campusId);
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        var result = bookingRepo.search(campuses, status == null ? "" : status.trim(),
                keyword == null ? "" : keyword.trim(), pageable);
        List<Map<String, Object>> list = result.getContent().stream()
                .map(classroomAdminService::toBookingAdminMap)
                .toList();
        return new PageResult<>(list, result.getTotalElements(), result.getNumber() + 1, result.getSize());
    }

    @Transactional
    public Map<String, Object> review(Long id, boolean approve, String reason) {
        PracticeRoomBooking booking = bookingRepo.findById(id).orElseThrow(() -> new BizException("预约不存在"));
        adminAccessService.assertCanAccessCampus(booking.getCampusId());
        if (!PENDING.equals(booking.getStatus())) {
            throw new BizException("仅待审核预约可处理");
        }
        if (approve) {
            if (roomAvailabilityService.isOccupiedByClassOrRental(
                    booking.getClassroomId(), booking.getClassDate(), booking.getStartTime(), booking.getEndTime())) {
                throw new BizException("该时段教室已被占用，无法同意");
            }
            booking.setStatus(APPROVED);
            booking.setRejectReason(null);
        } else {
            booking.setStatus(REJECTED);
            booking.setRejectReason(reason == null || reason.isBlank() ? "未通过" : reason.trim());
        }
        return classroomAdminService.toBookingAdminMap(bookingRepo.save(booking));
    }

    private Map<String, Object> toAppMap(PracticeRoomBooking booking) {
        Map<String, Object> map = classroomAdminService.toBookingAdminMap(booking);
        map.put("statusLabel", statusLabel(booking.getStatus()));
        return map;
    }

    private static String statusLabel(String status) {
        return switch (status == null ? "" : status) {
            case PENDING -> "待审核";
            case APPROVED -> "已同意";
            case REJECTED -> "已拒绝";
            case CANCELLED -> "已取消";
            default -> status;
        };
    }

    private static void validateDate(String date) {
        if (date == null || date.isBlank()) {
            throw new BizException("请选择日期");
        }
        try {
            LocalDate.parse(date);
        } catch (Exception e) {
            throw new BizException("日期格式不正确");
        }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Long longVal(Object value) {
        if (value == null || "".equals(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
