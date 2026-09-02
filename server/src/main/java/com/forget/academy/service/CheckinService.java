package com.forget.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.PracticeRecord;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckinService {
    public static final String SOURCE_SCAN = "scan";
    public static final String SOURCE_MANUAL = "manual";
    public static final String SOURCE_CONFIRMED = "confirmed";

    private final PracticeRecordRepo practiceRecordRepo;
    private final ScheduleRepo scheduleRepo;
    private final ObjectMapper mapper;
    private final UserCampusService userCampusService;
    private final UserCardService userCardService;

    @Transactional
    public Map<String, Object> checkin(Long userId, String raw) {
        Session session = parse(raw);
        if (practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(userId, session.id, session.date)) {
            throw new BizException("今日该课程已签到，请勿重复扫描");
        }
        Schedule schedule = scheduleRepo.findById(parseLong(session.id)).orElse(null);
        PracticeRecord record = buildRecord(userId, schedule, session, SOURCE_SCAN, null);
        saveRecord(record, "今日该课程已签到，请勿重复扫描");
        Long scheduleId = parseLong(session.id);
        if (scheduleId != null && scheduleId > 0) {
            userCardService.consumeOnClassCheckin(userId, scheduleId, session.date);
        }
        return Map.of("ok", true, "message", session.className + " 签到成功", "record", toMap(record));
    }

    @Transactional
    public Map<String, Object> manualCheckin(Long userId, Long scheduleId, String classDate, String operatorName) {
        return manualCheckin(userId, scheduleId, classDate, operatorName, SOURCE_MANUAL);
    }

    @Transactional
    public Map<String, Object> manualCheckin(Long userId, Long scheduleId, String classDate, String operatorName, String source) {
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        String date = normalizeDate(classDate);
        String sessionId = String.valueOf(scheduleId);
        if (practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(userId, sessionId, date)) {
            throw new BizException("该学员本节课已签到");
        }
        Session session = new Session(
                sessionId,
                schedule.getName(),
                date,
                schedule.getTimeText(),
                schedule.getTeacherName(),
                schedule.getRoom(),
                "75分钟");
        PracticeRecord record = buildRecord(userId, schedule, session, source, operatorName);
        saveRecord(record, "该学员本节课已签到");
        userCardService.consumeOnClassCheckin(userId, scheduleId, date);
        return Map.of("ok", true, "message", schedule.getName() + " 签到成功", "record", toMap(record));
    }

    public boolean hasCheckedIn(Long userId, Long scheduleId, String classDate) {
        return practiceRecordRepo.existsByUserIdAndSessionIdAndClassDate(
                userId, String.valueOf(scheduleId), normalizeDate(classDate));
    }

    public List<Map<String, Object>> myPractice(Long userId) {
        return practiceRecordRepo.findByUserIdOrderByCheckedAtDesc(userId).stream().map(this::toMap).toList();
    }

    public Map<String, String> resolveSession(String raw) {
        Session session = parse(raw);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("id", session.id);
        map.put("className", session.className);
        map.put("date", session.date);
        map.put("time", session.time);
        map.put("teacher", session.teacher);
        map.put("room", session.room);
        map.put("duration", session.duration);
        return map;
    }

    public Map<String, Object> payloadForSchedule(Long scheduleId, String date) {
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("t", "checkin");
        payload.put("id", String.valueOf(schedule.getId()));
        payload.put("className", schedule.getName());
        payload.put("date", normalizeDate(date));
        payload.put("time", schedule.getTimeText());
        payload.put("teacher", schedule.getTeacherName());
        payload.put("room", schedule.getRoom());
        payload.put("duration", "75分钟");
        try {
            return Map.of("payload", mapper.writeValueAsString(payload), "text", payload);
        } catch (Exception e) {
            throw new BizException("生成签到码失败");
        }
    }

    private PracticeRecord buildRecord(Long userId, Schedule schedule, Session session, String source, String operatorName) {
        PracticeRecord record = new PracticeRecord();
        record.setUserId(userId);
        record.setSessionId(session.id);
        record.setName(session.className);
        record.setClassDate(session.date);
        record.setTimeText(session.time);
        record.setDuration(session.duration);
        record.setTeacherName(session.teacher);
        record.setRoom(session.room);
        record.setCampusId(resolveCampusId(schedule, session.id));
        record.setCheckinSource(source);
        record.setOperatorName(operatorName);
        record.setCheckedAt(Instant.now());
        return record;
    }

    private void saveRecord(PracticeRecord record, String duplicateMessage) {
        try {
            practiceRecordRepo.save(record);
            userCampusService.ensureLinked(record.getUserId(), record.getCampusId());
        } catch (DataIntegrityViolationException e) {
            throw new BizException(duplicateMessage);
        }
    }

    private String resolveCampusId(Schedule schedule, String sessionId) {
        if (schedule != null && schedule.getCampusId() != null && !schedule.getCampusId().isBlank()) {
            return schedule.getCampusId();
        }
        Schedule loaded = scheduleRepo.findById(parseLong(sessionId)).orElse(null);
        if (loaded == null || loaded.getCampusId() == null || loaded.getCampusId().isBlank()) {
            return CampusIds.DEFAULT;
        }
        return loaded.getCampusId();
    }

    private String normalizeDate(String classDate) {
        return classDate == null || classDate.isBlank() ? LocalDate.now().toString() : classDate.trim();
    }

    private Session parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BizException("无法识别的签到码，请扫描教室二维码");
        }
        String text = raw.trim();
        try {
            JsonNode node = mapper.readTree(text);
            if ("checkin".equals(node.path("t").asText()) && node.has("id")) {
                return fromNode(node);
            }
        } catch (Exception ignored) {
            // not json
        }
        if (text.toUpperCase().startsWith("FORGET_CHECKIN:")) {
            String id = text.substring("FORGET_CHECKIN:".length());
            Schedule schedule = scheduleRepo.findById(parseLong(id)).orElse(null);
            if (schedule == null) {
                throw new BizException("无法识别的签到码，请扫描教室二维码");
            }
            return new Session(
                    String.valueOf(schedule.getId()),
                    schedule.getName(),
                    LocalDate.now().toString(),
                    schedule.getTimeText(),
                    schedule.getTeacherName(),
                    schedule.getRoom(),
                    "75分钟");
        }
        throw new BizException("无法识别的签到码，请扫描教室二维码");
    }

    private Session fromNode(JsonNode node) {
        String id = node.path("id").asText();
        Schedule preset = scheduleRepo.findById(parseLong(id)).orElse(null);
        return new Session(
                id,
                first(node.path("className").asText(), preset == null ? "课程" : preset.getName()),
                first(node.path("date").asText(), LocalDate.now().toString()),
                first(node.path("time").asText(), preset == null ? "" : preset.getTimeText()),
                first(node.path("teacher").asText(), preset == null ? "" : preset.getTeacherName()),
                first(node.path("room").asText(), preset == null ? "" : preset.getRoom()),
                first(node.path("duration").asText(), "60分钟"));
    }

    private Map<String, Object> toMap(PracticeRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("userId", record.getUserId());
        map.put("sessionId", record.getSessionId());
        map.put("name", record.getName());
        map.put("date", record.getClassDate());
        map.put("time", record.getTimeText());
        map.put("duration", record.getDuration());
        map.put("teacher", record.getTeacherName());
        map.put("room", record.getRoom());
        map.put("checkinSource", record.getCheckinSource());
        map.put("operatorName", record.getOperatorName());
        map.put("checkedAt", record.getCheckedAt() == null ? null : record.getCheckedAt().toEpochMilli());
        return map;
    }

    private static String first(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1L;
        }
    }

    private record Session(String id, String className, String date, String time, String teacher, String room, String duration) {}
}
