package com.forget.academy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forget.academy.entity.Course;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CourseModuleMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CourseModuleMapper() {
    }

    public static Map<String, Object> toModuleMap(Course course) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", course.getId());
        map.put("moduleType", course.getModuleType());
        map.put("moduleKey", course.getModuleKey());
        map.put("name", course.getName());
        map.put("summary", course.getSummary());
        map.put("tag", course.getTag());
        map.put("level", course.getLevel());
        map.put("desc", course.getDescription());
        map.put("cover", course.getCover());
        map.put("price", displayPrice(course));
        map.put("priceValue", course.getPrice());
        map.put("unit", course.getPriceUnit() == null || course.getPriceUnit().isBlank() ? "节" : course.getPriceUnit());
        map.put("highlights", parseHighlights(course.getHighlights()));
        map.put("actionLabel", course.getActionLabel());
        map.put("actionTab", course.getActionTab());
        map.put("customerServiceQr", course.getCustomerServiceQr());
        map.put("sortOrder", course.getSortOrder());
        map.put("enabled", course.getEnabled());
        return map;
    }

    public static String displayPrice(Course course) {
        if (course.getPriceDisplay() != null && !course.getPriceDisplay().isBlank()) {
            return course.getPriceDisplay().trim();
        }
        if (course.getPrice() == null) {
            return "";
        }
        return String.valueOf(course.getPrice());
    }

    public static List<String> parseHighlights(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String text = raw.trim();
        if (text.startsWith("[")) {
            try {
                return MAPPER.readValue(text, new TypeReference<>() {
                });
            } catch (Exception ignored) {
                // fall through
            }
        }
        List<String> items = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            String item = line.trim();
            if (item.startsWith("·")) {
                item = item.substring(1).trim();
            }
            if (item.startsWith("-")) {
                item = item.substring(1).trim();
            }
            if (!item.isBlank()) {
                items.add(item);
            }
        }
        return items;
    }

    public static String serializeHighlights(List<String> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return "";
        }
        return String.join("\n", highlights.stream().map(String::trim).filter(s -> !s.isBlank()).toList());
    }
}
