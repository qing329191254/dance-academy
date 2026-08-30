package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.Classroom;
import com.forget.academy.entity.ClassroomSlot;
import com.forget.academy.entity.PracticeRoomBooking;
import com.forget.academy.entity.RoomRental;
import com.forget.academy.repo.ClassroomRepo;
import com.forget.academy.repo.ClassroomSlotRepo;
import com.forget.academy.repo.PracticeRoomBookingRepo;
import com.forget.academy.repo.RoomRentalRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClassroomAdminService {
    private final ClassroomRepo classroomRepo;
    private final ClassroomSlotRepo classroomSlotRepo;
    private final RoomRentalRepo roomRentalRepo;
    private final PracticeRoomBookingRepo practiceRoomBookingRepo;
    private final AdminAccessService adminAccessService;
    private final CampusCatalogService campusCatalogService;

    public List<Map<String, Object>> listClassrooms(String campusId) {
        String campus = requireCampus(campusId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Classroom classroom : classroomRepo.findByCampusIdOrderBySortOrderAscIdAsc(campus)) {
            result.add(toClassroomMap(classroom, true));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> saveClassroom(String campusId, Map<String, Object> body) {
        String campus = requireCampus(campusId);
        Long id = longVal(body.get("id"));
        Classroom classroom = id == null ? new Classroom() : classroomRepo.findById(id)
                .orElseThrow(() -> new BizException("教室不存在"));
        if (classroom.getId() != null && !campus.equals(classroom.getCampusId())) {
            throw new BizException("无权修改其他校区教室");
        }
        String name = str(body.get("name"));
        if (name.isBlank()) {
            throw new BizException("请填写教室名称");
        }
        classroom.setCampusId(campus);
        classroom.setName(name);
        classroom.setShortName(str(body.get("shortName")));
        classroom.setAllowPractice(bool(body.get("allowPractice"), true));
        classroom.setAllowRental(bool(body.get("allowRental"), true));
        classroom.setEnabled(bool(body.get("enabled"), true));
        classroom.setSortOrder(intVal(body.get("sortOrder"), 0));
        Classroom saved = classroomRepo.save(classroom);
        replaceSlots(saved.getId(), body.get("slots"));
        return toClassroomMap(saved, true);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepo.findById(id).orElseThrow(() -> new BizException("教室不存在"));
        adminAccessService.assertCanAccessCampus(classroom.getCampusId());
        classroomSlotRepo.deleteByClassroomId(id);
        classroomRepo.delete(classroom);
    }

    public List<Map<String, Object>> listRentals(String campusId) {
        String campus = requireCampus(campusId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (RoomRental rental : roomRentalRepo.findByCampusIdOrderByClassDateDescIdDesc(campus)) {
            result.add(toRentalMap(rental));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> saveRental(String campusId, Map<String, Object> body) {
        String campus = requireCampus(campusId);
        Long id = longVal(body.get("id"));
        RoomRental rental = id == null ? new RoomRental() : roomRentalRepo.findById(id)
                .orElseThrow(() -> new BizException("租赁记录不存在"));
        Long classroomId = longVal(body.get("classroomId"));
        Classroom classroom = classroomRepo.findById(classroomId == null ? -1 : classroomId)
                .orElseThrow(() -> new BizException("请选择教室"));
        if (!campus.equals(classroom.getCampusId())) {
            throw new BizException("教室不属于当前校区");
        }
        String date = str(body.get("classDate"));
        String start = RoomAvailabilityService.normalizeHm(str(body.get("startTime")));
        String end = RoomAvailabilityService.normalizeHm(str(body.get("endTime")));
        if (date.isBlank()) {
            throw new BizException("请选择日期");
        }
        if (RoomAvailabilityService.minutes(end) <= RoomAvailabilityService.minutes(start)) {
            throw new BizException("结束时间需晚于开始时间");
        }
        rental.setCampusId(campus);
        rental.setClassroomId(classroom.getId());
        rental.setClassDate(date);
        rental.setStartTime(start);
        rental.setEndTime(end);
        rental.setContactName(str(body.get("contactName")));
        rental.setPhone(str(body.get("phone")));
        rental.setRemark(str(body.get("remark")));
        rental.setStatus("cancelled".equals(str(body.get("status"))) ? "cancelled" : "confirmed");
        return toRentalMap(roomRentalRepo.save(rental));
    }

    @Transactional
    public void cancelRental(Long id) {
        RoomRental rental = roomRentalRepo.findById(id).orElseThrow(() -> new BizException("租赁记录不存在"));
        adminAccessService.assertCanAccessCampus(rental.getCampusId());
        rental.setStatus("cancelled");
        roomRentalRepo.save(rental);
    }

    public Map<String, Object> toClassroomMap(Classroom classroom, boolean includeSlots) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", classroom.getId());
        map.put("campusId", classroom.getCampusId());
        map.put("campusName", campusCatalogService.displayName(classroom.getCampusId()));
        map.put("name", classroom.getName());
        map.put("shortName", classroom.getShortName());
        map.put("allowPractice", Boolean.TRUE.equals(classroom.getAllowPractice()));
        map.put("allowRental", Boolean.TRUE.equals(classroom.getAllowRental()));
        map.put("enabled", Boolean.TRUE.equals(classroom.getEnabled()));
        map.put("sortOrder", classroom.getSortOrder());
        if (includeSlots) {
            List<Map<String, Object>> slots = new ArrayList<>();
            for (ClassroomSlot slot : classroomSlotRepo.findByClassroomIdOrderBySortOrderAscIdAsc(classroom.getId())) {
                slots.add(toSlotMap(slot));
            }
            map.put("slots", slots);
        }
        return map;
    }

    public Map<String, Object> toSlotMap(ClassroomSlot slot) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", slot.getId());
        map.put("classroomId", slot.getClassroomId());
        map.put("startTime", slot.getStartTime());
        map.put("endTime", slot.getEndTime());
        map.put("label", slot.getStartTime() + "-" + slot.getEndTime());
        map.put("sortOrder", slot.getSortOrder());
        map.put("enabled", Boolean.TRUE.equals(slot.getEnabled()));
        return map;
    }

    public Map<String, Object> toRentalMap(RoomRental rental) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", rental.getId());
        map.put("campusId", rental.getCampusId());
        map.put("classroomId", rental.getClassroomId());
        classroomRepo.findById(rental.getClassroomId()).ifPresent(c -> map.put("classroomName", c.getName()));
        map.put("classDate", rental.getClassDate());
        map.put("startTime", rental.getStartTime());
        map.put("endTime", rental.getEndTime());
        map.put("contactName", rental.getContactName());
        map.put("phone", rental.getPhone());
        map.put("remark", rental.getRemark());
        map.put("status", rental.getStatus());
        return map;
    }

    public Map<String, Object> toBookingAdminMap(PracticeRoomBooking booking) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", booking.getId());
        map.put("userId", booking.getUserId());
        map.put("campusId", booking.getCampusId());
        map.put("campusName", campusCatalogService.displayName(booking.getCampusId()));
        map.put("classroomId", booking.getClassroomId());
        classroomRepo.findById(booking.getClassroomId()).ifPresent(c -> map.put("classroomName", c.getName()));
        map.put("slotId", booking.getSlotId());
        map.put("classDate", booking.getClassDate());
        map.put("startTime", booking.getStartTime());
        map.put("endTime", booking.getEndTime());
        map.put("timeText", booking.getStartTime() + "-" + booking.getEndTime());
        map.put("name", booking.getName());
        map.put("status", booking.getStatus());
        map.put("rejectReason", booking.getRejectReason());
        map.put("createdAt", booking.getCreatedAt());
        return map;
    }

    @SuppressWarnings("unchecked")
    private void replaceSlots(Long classroomId, Object raw) {
        classroomSlotRepo.deleteByClassroomId(classroomId);
        if (!(raw instanceof List<?> list)) {
            return;
        }
        int index = 0;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Map<String, Object> body = (Map<String, Object>) map;
            String start = RoomAvailabilityService.normalizeHm(str(body.get("startTime")));
            String end = RoomAvailabilityService.normalizeHm(str(body.get("endTime")));
            if (start.isBlank() || end.isBlank()) {
                continue;
            }
            if (RoomAvailabilityService.minutes(end) <= RoomAvailabilityService.minutes(start)) {
                throw new BizException("时段结束时间需晚于开始时间");
            }
            ClassroomSlot slot = new ClassroomSlot();
            slot.setClassroomId(classroomId);
            slot.setStartTime(start);
            slot.setEndTime(end);
            slot.setSortOrder(intVal(body.get("sortOrder"), index));
            slot.setEnabled(bool(body.get("enabled"), true));
            classroomSlotRepo.save(slot);
            index++;
        }
    }

    private String requireCampus(String campusId) {
        if (campusId == null || campusId.isBlank()) {
            throw new BizException("请选择校区");
        }
        adminAccessService.assertCanAccessCampus(campusId.trim());
        return campusId.trim();
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

    private static int intVal(Object value, int fallback) {
        if (value == null || "".equals(String.valueOf(value))) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private static boolean bool(Object value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }
}
