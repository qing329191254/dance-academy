package com.forget.academy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class WxSubscribeService {
    private static final Logger log = LoggerFactory.getLogger(WxSubscribeService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    private final WxAuthService wxAuthService;
    private final ObjectMapper mapper;

    @Value("${app.wx-subscribe-template-id:}")
    private String templateId;

    @Value("${app.wx-mp-state:formal}")
    private String mpState;

    public WxSubscribeService(WxAuthService wxAuthService, ObjectMapper mapper) {
        this.wxAuthService = wxAuthService;
        this.mapper = mapper;
    }

    @Async
    public void sendBookingNotice(String openid, Booking booking) {
        if (templateId == null || templateId.isBlank() || openid == null || openid.isBlank() || openid.startsWith("dev_")) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("touser", openid);
            payload.put("template_id", templateId);
            payload.put("page", "pages/book/book");
            payload.put("miniprogram_state", mpState);
            payload.put("lang", "zh_CN");
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("thing25", field(clip(booking.getName(), 20)));
            data.put("time27", field(formatClassTime(booking.getClassDate(), booking.getTimeText())));
            data.put("thing24", field(clip(defaultText(booking.getTeacherName(), "授课老师"), 20)));
            data.put("thing28", field(clip(defaultText(booking.getRoom(), "教室见课表"), 20)));
            data.put("thing9", field("请提前10分钟到场热身"));
            payload.put("data", data);
            send(payload, true);
        } catch (Exception e) {
            log.warn("发送预约订阅消息失败: {}", e.getMessage());
        }
    }

    private void send(Map<String, Object> payload, boolean retryOnToken) throws Exception {
        String token = wxAuthService.getAccessToken();
        String query = "access_token=" + token;
        String body = mapper.writeValueAsString(payload);
        JsonNode node = mapper.readTree(wxAuthService.postJsonFirstOk(
                body,
                "http://api.weixin.qq.com/cgi-bin/message/subscribe/send?" + query,
                "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?" + query
        ));
        int errcode = node.path("errcode").asInt(0);
        if (errcode == 0) {
            log.info("预约订阅消息发送成功");
            return;
        }
        if (retryOnToken && (errcode == 40001 || errcode == 42001)) {
            wxAuthService.invalidateAccessToken();
            send(payload, false);
            return;
        }
        log.warn("微信订阅消息返回失败: {} {}", errcode, node.path("errmsg").asText());
    }

    private static Map<String, String> field(String value) {
        return Map.of("value", value);
    }

    private static String formatClassTime(String classDate, String timeText) {
        String clock = startClock(timeText);
        if (classDate != null && !classDate.isBlank() && !"default".equals(classDate)) {
            try {
                return LocalDate.parse(classDate).format(DATE_FMT) + " " + clock;
            } catch (Exception ignored) {
                return classDate + " " + clock;
            }
        }
        return clock;
    }

    private static String startClock(String timeText) {
        if (timeText == null || timeText.isBlank()) {
            return "00:00";
        }
        String text = timeText.trim();
        int dash = text.indexOf('-');
        if (dash > 0) {
            text = text.substring(0, dash).trim();
        }
        if (text.matches("\\d{1,2}:\\d{2}")) {
            String[] parts = text.split(":");
            return String.format("%02d:%s", Integer.parseInt(parts[0]), parts[1]);
        }
        return text.length() > 20 ? text.substring(0, 20) : text;
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String clip(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
