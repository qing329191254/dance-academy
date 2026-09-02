package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.DanceCategory;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.UserCard;
import com.forget.academy.repo.DanceCategoryRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.UserCardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DanceCategoryService {
    private final DanceCategoryRepo danceCategoryRepo;
    private final ScheduleRepo scheduleRepo;
    private final UserCardRepo userCardRepo;

    @Transactional
    public void ensureDefaults() {
        if (danceCategoryRepo.count() > 0) {
            return;
        }
        DanceCategory street = saveSection("街舞板块", "street", 1);
        DanceCategory academic = saveSection("学院派舞蹈板块", "academic", 2);
        DanceCategory shape = saveSection("塑形板块", "shape", 3);

        saveStyle(street.getId(), "Jazz", "jazz", 1);
        saveStyle(street.getId(), "HipHop", "hiphop", 2);
        saveStyle(street.getId(), "编舞", "choreography", 3);
        saveStyle(street.getId(), "韩舞", "kpop", 4);
        saveStyle(street.getId(), "Heels", "heels", 5);

        saveStyle(academic.getId(), "现代舞", "modern", 1);
        saveStyle(academic.getId(), "中国舞", "chinese", 2);
        saveStyle(academic.getId(), "芭蕾舞", "ballet", 3);
        saveStyle(academic.getId(), "作品集", "portfolio", 4);

        saveStyle(shape.getId(), "瑜伽", "yoga", 1);
        saveStyle(shape.getId(), "普拉提", "pilates", 2);
        saveStyle(shape.getId(), "私教", "private-shape", 3);
    }

    public List<Map<String, Object>> tree(boolean includeDisabled) {
        ensureDefaults();
        List<DanceCategory> sections = includeDisabled
                ? danceCategoryRepo.findByParentIdIsNullOrderBySortOrderAscIdAsc()
                : danceCategoryRepo.findByParentIdIsNullAndEnabledTrueOrderBySortOrderAscIdAsc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (DanceCategory section : sections) {
            Map<String, Object> row = toMap(section);
            List<DanceCategory> styles = includeDisabled
                    ? danceCategoryRepo.findByParentIdOrderBySortOrderAscIdAsc(section.getId())
                    : danceCategoryRepo.findByParentIdAndEnabledTrueOrderBySortOrderAscIdAsc(section.getId());
            row.put("children", styles.stream().map(this::toMap).toList());
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> listSections(boolean enabledOnly) {
        ensureDefaults();
        List<DanceCategory> sections = enabledOnly
                ? danceCategoryRepo.findByParentIdIsNullAndEnabledTrueOrderBySortOrderAscIdAsc()
                : danceCategoryRepo.findByParentIdIsNullOrderBySortOrderAscIdAsc();
        return sections.stream().map(this::toMap).toList();
    }

    public DanceCategory requireSection(Long id) {
        DanceCategory category = danceCategoryRepo.findById(id)
                .orElseThrow(() -> new BizException("板块不存在"));
        if (category.getParentId() != null) {
            throw new BizException("请选择板块，而不是舞种");
        }
        return category;
    }

    public DanceCategory requireStyle(Long id) {
        DanceCategory category = danceCategoryRepo.findById(id)
                .orElseThrow(() -> new BizException("舞种不存在"));
        if (category.getParentId() == null) {
            throw new BizException("请选择舞种，而不是板块");
        }
        return category;
    }

    public String nameOf(Long id) {
        if (id == null) {
            return null;
        }
        return danceCategoryRepo.findById(id).map(DanceCategory::getName).orElse(null);
    }

    @Transactional
    public DanceCategory create(DanceCategory body) {
        String name = normalizeName(body.getName());
        String code = normalizeCode(body.getCode(), name);
        if (danceCategoryRepo.existsByCode(code)) {
            throw new BizException("标识已存在");
        }
        Long parentId = body.getParentId();
        if (parentId != null) {
            requireSection(parentId);
        }
        DanceCategory category = new DanceCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setCode(code);
        category.setSortOrder(body.getSortOrder() == null ? 0 : body.getSortOrder());
        category.setEnabled(body.getEnabled() == null || body.getEnabled());
        return danceCategoryRepo.save(category);
    }

    @Transactional
    public DanceCategory update(Long id, DanceCategory body) {
        DanceCategory category = danceCategoryRepo.findById(id)
                .orElseThrow(() -> new BizException("记录不存在"));
        String name = normalizeName(body.getName());
        String code = normalizeCode(body.getCode(), name);
        if (danceCategoryRepo.existsByCodeAndIdNot(code, id)) {
            throw new BizException("标识已存在");
        }
        if (body.getParentId() != null && category.getParentId() == null) {
            throw new BizException("板块不能改为舞种");
        }
        if (body.getParentId() == null && category.getParentId() != null) {
            throw new BizException("舞种不能改为板块");
        }
        if (body.getParentId() != null) {
            requireSection(body.getParentId());
            category.setParentId(body.getParentId());
        }
        category.setName(name);
        category.setCode(code);
        if (body.getSortOrder() != null) {
            category.setSortOrder(body.getSortOrder());
        }
        if (body.getEnabled() != null) {
            category.setEnabled(body.getEnabled());
        }
        return danceCategoryRepo.save(category);
    }

    @Transactional
    public void delete(Long id) {
        DanceCategory category = danceCategoryRepo.findById(id)
                .orElseThrow(() -> new BizException("记录不存在"));
        if (category.getParentId() == null) {
            long children = danceCategoryRepo.countByParentId(id);
            if (children > 0) {
                throw new BizException("请先删除或移走该板块下的舞种，或改为停用");
            }
        }
        long usedBySchedule = scheduleRepo.findAll().stream()
                .filter(s -> id.equals(s.getSectionId()) || id.equals(s.getStyleId()))
                .count();
        if (usedBySchedule > 0) {
            throw new BizException("仍有 " + usedBySchedule + " 条课表在使用，无法删除，可改为停用");
        }
        long usedByCard = userCardRepo.findAll().stream()
                .filter(c -> id.equals(c.getSectionId()))
                .count();
        if (usedByCard > 0) {
            throw new BizException("仍有 " + usedByCard + " 张卡在使用，无法删除，可改为停用");
        }
        danceCategoryRepo.delete(category);
    }

    public Map<String, Object> toMap(DanceCategory category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", category.getId());
        map.put("parentId", category.getParentId());
        map.put("name", category.getName());
        map.put("code", category.getCode());
        map.put("sortOrder", category.getSortOrder());
        map.put("enabled", category.getEnabled());
        map.put("section", category.getParentId() == null);
        return map;
    }

    private DanceCategory saveSection(String name, String code, int sort) {
        DanceCategory category = new DanceCategory();
        category.setName(name);
        category.setCode(code);
        category.setSortOrder(sort);
        category.setEnabled(true);
        return danceCategoryRepo.save(category);
    }

    private void saveStyle(Long parentId, String name, String code, int sort) {
        DanceCategory category = new DanceCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setCode(code);
        category.setSortOrder(sort);
        category.setEnabled(true);
        danceCategoryRepo.save(category);
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BizException("请填写名称");
        }
        return name.trim();
    }

    private static String normalizeCode(String code, String name) {
        String raw = code == null || code.isBlank() ? name : code;
        String normalized = raw.trim().toLowerCase().replaceAll("[^a-z0-9\\-_]+", "-");
        if (normalized.isBlank()) {
            throw new BizException("请填写有效标识");
        }
        return normalized;
    }
}
