package com.forget.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.common.BizException;
import com.forget.academy.common.CampusIds;
import com.forget.academy.common.QrCodeUtil;
import com.forget.academy.entity.CheckinSession;
import com.forget.academy.entity.Schedule;
import com.forget.academy.repo.CheckinSessionRepo;
import com.forget.academy.repo.ScheduleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckinSessionService {
    private static final int QR_TTL_SECONDS = 60;
    private static final int QR_GRACE_SECONDS = 30;

    private final CheckinSessionRepo checkinSessionRepo;
    private final ScheduleRepo scheduleRepo;
    private final ObjectMapper mapper;

    @Transactional
    public CheckinSession openSession(Long operatorUserId, Long scheduleId, String classDate) {
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow(() -> new BizException("课表不存在"));
        String date = normalizeDate(classDate);
        closeActiveSessions(scheduleId, date);
        CheckinSession session = new CheckinSession();
        session.setScheduleId(scheduleId);
        session.setClassDate(date);
        session.setCampusId(resolveCampus(schedule));
        session.setSessionToken(UUID.randomUUID().toString().replace("-", ""));
        session.setActive(true);
        session.setOpenedByUserId(operatorUserId);
        return checkinSessionRepo.save(session);
    }

    public Map<String, Object> toSessionMapPublic(CheckinSession session) {
        return toSessionMap(session);
    }

    @Transactional
    public void closeSession(Long sessionId) {
        checkinSessionRepo.findById(sessionId).ifPresent(session -> {
            session.setActive(false);
            session.setClosedAt(Instant.now());
            checkinSessionRepo.save(session);
        });
    }

    public Map<String, Object> getActiveSession(Long scheduleId, String classDate) {
        String date = normalizeDate(classDate);
        return checkinSessionRepo.findFirstByScheduleIdAndClassDateAndActiveTrueOrderByIdDesc(scheduleId, date)
                .map(this::toSessionMap)
                .orElse(null);
    }

    public Map<String, Object> buildQrPayload(Long sessionId) {
        CheckinSession session = checkinSessionRepo.findById(sessionId)
                .orElseThrow(() -> new BizException("签到场次不存在"));
        if (!Boolean.TRUE.equals(session.getActive())) {
            throw new BizException("签到已结束，请工作人员重新开启");
        }
        Schedule schedule = scheduleRepo.findById(session.getScheduleId())
                .orElseThrow(() -> new BizException("课表不存在"));
        long exp = Instant.now().getEpochSecond() + QR_TTL_SECONDS;
        String sig = sign(session, exp);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("t", "checkin");
        payload.put("id", String.valueOf(session.getScheduleId()));
        payload.put("date", session.getClassDate());
        payload.put("sid", session.getId());
        payload.put("exp", exp);
        payload.put("sig", sig);
        payload.put("className", schedule.getName());
        payload.put("time", schedule.getTimeText());
        payload.put("teacher", schedule.getTeacherName());
        payload.put("room", schedule.getRoom());
        payload.put("duration", "75分钟");
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("sessionId", session.getId());
            String payloadJson = mapper.writeValueAsString(payload);
            result.put("payload", payloadJson);
            result.put("text", payload);
            result.put("expiresIn", QR_TTL_SECONDS);
            result.put("qrDataUrl", QrCodeUtil.toDataUrl(payloadJson, 320));
            return result;
        } catch (Exception e) {
            throw new BizException("生成签到码失败");
        }
    }

    public List<Map<String, Object>> listSchedulesForCampus(String campusId, String classDate) {
        String date = normalizeDate(classDate);
        int weekday = toWeekday(LocalDate.parse(date));
        String campus = campusId == null || campusId.isBlank() ? CampusIds.DEFAULT : campusId.trim();
        List<Schedule> schedules = scheduleRepo.findAllByOrderByTypeAscSortOrderAscIdAsc();
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Schedule schedule : schedules) {
            if (!Boolean.TRUE.equals(schedule.getEnabled())) {
                continue;
            }
            if (!campus.equals(resolveCampus(schedule))) {
                continue;
            }
            if ("group".equals(schedule.getType())
                    && schedule.getWeekday() != null
                    && !schedule.getWeekday().equals(weekday)) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", schedule.getId());
            row.put("name", schedule.getName());
            row.put("timeText", schedule.getTimeText());
            row.put("teacherName", schedule.getTeacherName());
            row.put("room", schedule.getRoom());
            row.put("type", schedule.getType());
            row.put("date", date);
            Map<String, Object> active = getActiveSession(schedule.getId(), date);
            row.put("activeSession", active);
            result.add(row);
        }
        return result;
    }

    public ValidatedScan validateScanPayload(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BizException("无法识别的签到码，请扫描工作人员展示的二维码");
        }
        JsonNode node = parseJson(raw.trim());
        if (node == null || !"checkin".equals(node.path("t").asText())) {
            throw new BizException("无法识别的签到码，请扫描工作人员展示的二维码");
        }
        Long sessionId = parseLong(node.path("sid"));
        long exp = node.path("exp").asLong(0);
        String sig = node.path("sig").asText("");
        if (sessionId == null || exp <= 0 || sig.isBlank()) {
            throw new BizException("签到码无效，请让工作人员刷新后重扫");
        }
        CheckinSession session = checkinSessionRepo.findById(sessionId)
                .orElseThrow(() -> new BizException("签到场次不存在或已结束"));
        if (!Boolean.TRUE.equals(session.getActive())) {
            throw new BizException("签到已结束，请联系工作人员重新开启");
        }
        long now = Instant.now().getEpochSecond();
        if (now > exp + QR_GRACE_SECONDS) {
            throw new BizException("签到码已过期，请让工作人员刷新后重扫");
        }
        if (now < exp - QR_TTL_SECONDS - QR_GRACE_SECONDS) {
            throw new BizException("签到码无效，请重新扫描");
        }
        String expected = sign(session, exp);
        if (!expected.equals(sig)) {
            throw new BizException("签到码校验失败，请重新扫描");
        }
        String scheduleIdText = node.path("id").asText(String.valueOf(session.getScheduleId()));
        if (!String.valueOf(session.getScheduleId()).equals(scheduleIdText)) {
            throw new BizException("签到码与课程不匹配");
        }
        String date = node.path("date").asText(session.getClassDate());
        if (!session.getClassDate().equals(date)) {
            throw new BizException("签到码日期不匹配");
        }
        Schedule schedule = scheduleRepo.findById(session.getScheduleId())
                .orElseThrow(() -> new BizException("课表不存在"));
        return new ValidatedScan(session, schedule, date);
    }

    private void closeActiveSessions(Long scheduleId, String classDate) {
        List<CheckinSession> active = checkinSessionRepo.findByScheduleIdAndClassDateAndActiveTrue(scheduleId, classDate);
        Instant now = Instant.now();
        for (CheckinSession item : active) {
            item.setActive(false);
            item.setClosedAt(now);
            checkinSessionRepo.save(item);
        }
    }

    private Map<String, Object> toSessionMap(CheckinSession session) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", session.getId());
        map.put("scheduleId", session.getScheduleId());
        map.put("classDate", session.getClassDate());
        map.put("campusId", session.getCampusId());
        map.put("active", session.getActive());
        map.put("openedAt", session.getCreatedAt() == null ? null : session.getCreatedAt().toEpochMilli());
        scheduleRepo.findById(session.getScheduleId()).ifPresent(schedule -> {
            map.put("className", schedule.getName());
            map.put("timeText", schedule.getTimeText());
            map.put("teacherName", schedule.getTeacherName());
            map.put("room", schedule.getRoom());
        });
        return map;
    }

    private String sign(CheckinSession session, long exp) {
        String base = session.getId() + ":" + session.getScheduleId() + ":" + session.getClassDate() + ":" + exp;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(session.getSessionToken().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(base.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            throw new BizException("签到码签名失败");
        }
    }

    private JsonNode parseJson(String raw) {
        try {
            return mapper.readTree(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeDate(String classDate) {
        return classDate == null || classDate.isBlank() ? LocalDate.now().toString() : classDate.trim();
    }

    private static String resolveCampus(Schedule schedule) {
        if (schedule.getCampusId() == null || schedule.getCampusId().isBlank()) {
            return CampusIds.DEFAULT;
        }
        return schedule.getCampusId();
    }

    private static Long parseLong(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        try {
            return Long.parseLong(node.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int toWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SUNDAY ? 0 : day.getValue();
    }

    public record ValidatedScan(CheckinSession session, Schedule schedule, String classDate) {}
}
