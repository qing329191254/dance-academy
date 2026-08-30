package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.common.PageResult;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Survey;
import com.forget.academy.entity.SurveyAnswer;
import com.forget.academy.entity.SurveyOption;
import com.forget.academy.entity.SurveyQuestion;
import com.forget.academy.entity.SurveyResponse;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.SurveyAnswerRepo;
import com.forget.academy.repo.SurveyOptionRepo;
import com.forget.academy.repo.SurveyQuestionRepo;
import com.forget.academy.repo.SurveyRepo;
import com.forget.academy.repo.SurveyResponseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_SINGLE = "single";
    public static final String TYPE_MULTI = "multi";

    private final SurveyRepo surveyRepo;
    private final SurveyQuestionRepo questionRepo;
    private final SurveyOptionRepo optionRepo;
    private final SurveyResponseRepo responseRepo;
    private final SurveyAnswerRepo answerRepo;
    private final AppUserRepo appUserRepo;
    private final AdminAccessService adminAccessService;
    private final CampusCatalogService campusCatalogService;

    public List<Map<String, Object>> adminList(String campusId) {
        String campus = requireCampus(campusId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Survey survey : surveyRepo.findByCampusIdOrderBySortOrderAscIdDesc(campus)) {
            Map<String, Object> map = toSurveyMap(survey, true);
            map.put("responseCount", responseRepo.countBySurveyId(survey.getId()));
            result.add(map);
        }
        return result;
    }

    public Map<String, Object> adminDetail(Long id) {
        Survey survey = surveyRepo.findById(id).orElseThrow(() -> new BizException("问卷不存在"));
        adminAccessService.assertCanAccessCampus(survey.getCampusId());
        Map<String, Object> map = toSurveyMap(survey, true);
        map.put("responseCount", responseRepo.countBySurveyId(survey.getId()));
        return map;
    }

    @Transactional
    public Map<String, Object> saveSurvey(String campusId, Map<String, Object> body) {
        String campus = requireCampus(campusId);
        Long id = longVal(body.get("id"));
        Survey survey = id == null ? new Survey() : surveyRepo.findById(id)
                .orElseThrow(() -> new BizException("问卷不存在"));
        if (survey.getId() != null && !campus.equals(survey.getCampusId())) {
            throw new BizException("无权修改其他校区问卷");
        }
        String title = str(body.get("title"));
        if (title.isBlank()) {
            throw new BizException("请填写问卷标题");
        }
        survey.setCampusId(campus);
        survey.setTitle(title);
        survey.setDescription(str(body.get("description")));
        survey.setEnabled(bool(body.get("enabled"), true));
        survey.setSortOrder(intVal(body.get("sortOrder"), 0));
        Survey saved = surveyRepo.save(survey);
        replaceQuestions(saved.getId(), body.get("questions"));
        return toSurveyMap(saved, true);
    }

    @Transactional
    public void deleteSurvey(Long id) {
        Survey survey = surveyRepo.findById(id).orElseThrow(() -> new BizException("问卷不存在"));
        adminAccessService.assertCanAccessCampus(survey.getCampusId());
        List<SurveyResponse> responses = responseRepo.findBySurveyId(id);
        for (SurveyResponse response : responses) {
            answerRepo.deleteByResponseId(response.getId());
            responseRepo.delete(response);
        }
        clearQuestions(id);
        surveyRepo.delete(survey);
    }

    public PageResult<Map<String, Object>> adminResponses(Long surveyId, String keyword, int page, int size) {
        Survey survey = surveyRepo.findById(surveyId).orElseThrow(() -> new BizException("问卷不存在"));
        adminAccessService.assertCanAccessCampus(survey.getCampusId());
        var campuses = adminAccessService.resolveCampusScope(survey.getCampusId());
        var pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "id"));
        var result = responseRepo.search(surveyId, campuses, keyword == null ? "" : keyword.trim(), pageable);
        List<Map<String, Object>> list = result.getContent().stream()
                .map(item -> toResponseMap(item, true))
                .toList();
        return new PageResult<>(list, result.getTotalElements(), result.getNumber() + 1, result.getSize());
    }

    public List<Map<String, Object>> appList(String campusId, Long userId) {
        String campus = campusCatalogService.normalize(campusId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Survey survey : surveyRepo.findByCampusIdAndEnabledTrueOrderBySortOrderAscIdDesc(campus)) {
            Map<String, Object> map = toSurveyBrief(survey);
            boolean submitted = userId != null && responseRepo.existsBySurveyIdAndUserId(survey.getId(), userId);
            map.put("submitted", submitted);
            result.add(map);
        }
        return result;
    }

    public Map<String, Object> appDetail(Long id, Long userId) {
        Survey survey = surveyRepo.findById(id).orElseThrow(() -> new BizException("问卷不存在"));
        if (!Boolean.TRUE.equals(survey.getEnabled())) {
            throw new BizException("问卷已关闭");
        }
        Map<String, Object> map = toSurveyMap(survey, true);
        boolean submitted = userId != null && responseRepo.existsBySurveyIdAndUserId(id, userId);
        map.put("submitted", submitted);
        if (submitted) {
            responseRepo.findBySurveyIdAndUserId(id, userId).ifPresent(response ->
                    map.put("myResponse", toResponseMap(response, true)));
        }
        return map;
    }

    @Transactional
    public Map<String, Object> submit(Long userId, Map<String, Object> body) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        Long surveyId = longVal(body.get("surveyId"));
        Survey survey = surveyRepo.findById(surveyId == null ? -1 : surveyId)
                .orElseThrow(() -> new BizException("问卷不存在"));
        if (!Boolean.TRUE.equals(survey.getEnabled())) {
            throw new BizException("问卷已关闭");
        }
        if (responseRepo.existsBySurveyIdAndUserId(survey.getId(), userId)) {
            throw new BizException("你已填写过该问卷");
        }
        List<SurveyQuestion> questions = questionRepo.findBySurveyIdOrderBySortOrderAscIdAsc(survey.getId());
        if (questions.isEmpty()) {
            throw new BizException("问卷暂无题目");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = body.get("answers") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        Map<Long, Map<String, Object>> answerByQuestion = new HashMap<>();
        for (Map<String, Object> answer : answers) {
            Long questionId = longVal(answer.get("questionId"));
            if (questionId != null) {
                answerByQuestion.put(questionId, answer);
            }
        }

        SurveyResponse response = new SurveyResponse();
        response.setSurveyId(survey.getId());
        response.setUserId(userId);
        response.setNickname(user.getNickname() == null ? "" : user.getNickname());
        response.setCampusId(survey.getCampusId());
        SurveyResponse saved = responseRepo.save(response);

        for (SurveyQuestion question : questions) {
            Map<String, Object> answerBody = answerByQuestion.get(question.getId());
            SurveyAnswer answer = buildAnswer(saved.getId(), question, answerBody);
            answerRepo.save(answer);
        }
        return toResponseMap(saved, true);
    }

    private SurveyAnswer buildAnswer(Long responseId, SurveyQuestion question, Map<String, Object> answerBody) {
        String type = question.getType() == null ? TYPE_TEXT : question.getType();
        boolean required = Boolean.TRUE.equals(question.getRequired());
        SurveyAnswer answer = new SurveyAnswer();
        answer.setResponseId(responseId);
        answer.setQuestionId(question.getId());
        if (TYPE_TEXT.equals(type)) {
            String text = answerBody == null ? "" : str(answerBody.get("textValue"));
            if (required && text.isBlank()) {
                throw new BizException("请填写：" + question.getTitle());
            }
            answer.setTextValue(text);
            answer.setOptionIds("");
            return answer;
        }
        List<Long> selected = parseOptionIds(answerBody);
        if (required && selected.isEmpty()) {
            throw new BizException("请选择：" + question.getTitle());
        }
        Set<Long> allowed = optionRepo.findByQuestionIdOrderBySortOrderAscIdAsc(question.getId()).stream()
                .map(SurveyOption::getId)
                .collect(Collectors.toSet());
        for (Long optionId : selected) {
            if (!allowed.contains(optionId)) {
                throw new BizException("选项无效：" + question.getTitle());
            }
        }
        if (TYPE_SINGLE.equals(type) && selected.size() > 1) {
            throw new BizException("单选题只能选一项：" + question.getTitle());
        }
        answer.setTextValue("");
        answer.setOptionIds(selected.stream().map(String::valueOf).collect(Collectors.joining(",")));
        return answer;
    }

    private List<Long> parseOptionIds(Map<String, Object> answerBody) {
        if (answerBody == null) {
            return List.of();
        }
        Object raw = answerBody.get("optionIds");
        if (raw == null) {
            raw = answerBody.get("optionId");
        }
        List<Long> result = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                Long id = longVal(item);
                if (id != null) {
                    result.add(id);
                }
            }
            return result;
        }
        String text = str(raw);
        if (text.isBlank()) {
            return result;
        }
        for (String part : text.split(",")) {
            Long id = longVal(part.trim());
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private void replaceQuestions(Long surveyId, Object raw) {
        clearQuestions(surveyId);
        if (!(raw instanceof List<?> list)) {
            return;
        }
        int index = 0;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) map;
            String title = str(body.get("title"));
            if (title.isBlank()) {
                continue;
            }
            String type = str(body.get("type"));
            if (!TYPE_TEXT.equals(type) && !TYPE_SINGLE.equals(type) && !TYPE_MULTI.equals(type)) {
                type = TYPE_TEXT;
            }
            SurveyQuestion question = new SurveyQuestion();
            question.setSurveyId(surveyId);
            question.setTitle(title);
            question.setType(type);
            question.setRequired(bool(body.get("required"), true));
            question.setSortOrder(intVal(body.get("sortOrder"), index));
            SurveyQuestion saved = questionRepo.save(question);
            if (!TYPE_TEXT.equals(type)) {
                replaceOptions(saved.getId(), body.get("options"));
            }
            index++;
        }
    }

    private void replaceOptions(Long questionId, Object raw) {
        optionRepo.deleteByQuestionId(questionId);
        if (!(raw instanceof List<?> list)) {
            return;
        }
        int index = 0;
        for (Object item : list) {
            String label;
            if (item instanceof Map<?, ?> map) {
                label = str(map.get("label"));
            } else {
                label = str(item);
            }
            if (label.isBlank()) {
                continue;
            }
            SurveyOption option = new SurveyOption();
            option.setQuestionId(questionId);
            option.setLabel(label);
            option.setSortOrder(index);
            optionRepo.save(option);
            index++;
        }
        if (index == 0) {
            throw new BizException("选择题至少需要一个选项");
        }
    }

    private void clearQuestions(Long surveyId) {
        for (SurveyQuestion question : questionRepo.findBySurveyIdOrderBySortOrderAscIdAsc(surveyId)) {
            optionRepo.deleteByQuestionId(question.getId());
        }
        questionRepo.deleteBySurveyId(surveyId);
    }

    private Map<String, Object> toSurveyBrief(Survey survey) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", survey.getId());
        map.put("campusId", survey.getCampusId());
        map.put("campusName", campusCatalogService.displayName(survey.getCampusId()));
        map.put("title", survey.getTitle());
        map.put("description", survey.getDescription());
        map.put("enabled", Boolean.TRUE.equals(survey.getEnabled()));
        map.put("sortOrder", survey.getSortOrder());
        return map;
    }

    private Map<String, Object> toSurveyMap(Survey survey, boolean includeQuestions) {
        Map<String, Object> map = toSurveyBrief(survey);
        if (includeQuestions) {
            List<Map<String, Object>> questions = new ArrayList<>();
            for (SurveyQuestion question : questionRepo.findBySurveyIdOrderBySortOrderAscIdAsc(survey.getId())) {
                questions.add(toQuestionMap(question));
            }
            map.put("questions", questions);
        }
        return map;
    }

    private Map<String, Object> toQuestionMap(SurveyQuestion question) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", question.getId());
        map.put("surveyId", question.getSurveyId());
        map.put("type", question.getType());
        map.put("title", question.getTitle());
        map.put("required", Boolean.TRUE.equals(question.getRequired()));
        map.put("sortOrder", question.getSortOrder());
        List<Map<String, Object>> options = new ArrayList<>();
        for (SurveyOption option : optionRepo.findByQuestionIdOrderBySortOrderAscIdAsc(question.getId())) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", option.getId());
            row.put("label", option.getLabel());
            row.put("sortOrder", option.getSortOrder());
            options.add(row);
        }
        map.put("options", options);
        return map;
    }

    private Map<String, Object> toResponseMap(SurveyResponse response, boolean includeAnswers) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", response.getId());
        map.put("surveyId", response.getSurveyId());
        map.put("userId", response.getUserId());
        map.put("nickname", response.getNickname());
        map.put("campusId", response.getCampusId());
        map.put("campusName", campusCatalogService.displayName(response.getCampusId()));
        map.put("createdAt", response.getCreatedAt());
        if (includeAnswers) {
            Map<Long, SurveyQuestion> questionMap = questionRepo
                    .findBySurveyIdOrderBySortOrderAscIdAsc(response.getSurveyId()).stream()
                    .collect(Collectors.toMap(SurveyQuestion::getId, q -> q, (a, b) -> a, LinkedHashMap::new));
            Map<Long, String> optionLabelMap = new HashMap<>();
            for (SurveyQuestion question : questionMap.values()) {
                for (SurveyOption option : optionRepo.findByQuestionIdOrderBySortOrderAscIdAsc(question.getId())) {
                    optionLabelMap.put(option.getId(), option.getLabel());
                }
            }
            List<Map<String, Object>> answers = new ArrayList<>();
            for (SurveyAnswer answer : answerRepo.findByResponseIdOrderByIdAsc(response.getId())) {
                SurveyQuestion question = questionMap.get(answer.getQuestionId());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("questionId", answer.getQuestionId());
                row.put("questionTitle", question == null ? "" : question.getTitle());
                row.put("type", question == null ? "" : question.getType());
                row.put("textValue", answer.getTextValue());
                List<Long> optionIds = Arrays.stream(str(answer.getOptionIds()).split(","))
                        .map(this::longVal)
                        .filter(Objects::nonNull)
                        .toList();
                row.put("optionIds", optionIds);
                row.put("optionLabels", optionIds.stream()
                        .map(optionLabelMap::get)
                        .filter(Objects::nonNull)
                        .toList());
                answers.add(row);
            }
            map.put("answers", answers);
        }
        return map;
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

    private Long longVal(Object value) {
        if (value == null || "".equals(String.valueOf(value).trim())) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static int intVal(Object value, int fallback) {
        if (value == null || "".equals(String.valueOf(value).trim())) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
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
        String text = String.valueOf(value);
        return "true".equalsIgnoreCase(text) || "1".equals(text);
    }
}
