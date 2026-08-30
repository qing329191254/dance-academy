package com.forget.academy.service;

import com.forget.academy.common.BizException;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Teacher;
import com.forget.academy.entity.TeacherResumeMedia;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.repo.TeacherResumeMediaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeacherResumeService {
    public static final String PHOTO = "photo";
    public static final String VIDEO = "video";
    private static final int MAX_PHOTOS = 12;
    private static final int MAX_VIDEOS = 6;

    private final TeacherService teacherService;
    private final TeacherRepo teacherRepo;
    private final TeacherResumeMediaRepo mediaRepo;

    public Map<String, Object> myResume(Long userId) {
        AppUser user = teacherService.requireTeacherUser(userId);
        Teacher teacher = teacherRepo.findById(user.getTeacherId())
                .orElseThrow(() -> new BizException("老师档案不存在"));
        return toResumeMap(teacher);
    }

    public Map<String, Object> adminResume(Long teacherId) {
        Teacher teacher = teacherRepo.findById(teacherId).orElseThrow(() -> new BizException("老师不存在"));
        return toResumeMap(teacher);
    }

    @Transactional
    public Map<String, Object> saveMyResume(Long userId, Map<String, Object> body) {
        AppUser user = teacherService.requireTeacherUser(userId);
        Teacher teacher = teacherRepo.findById(user.getTeacherId())
                .orElseThrow(() -> new BizException("老师档案不存在"));
        String intro = str(body.get("resumeIntro"));
        if (intro.length() > 4000) {
            throw new BizException("自我介绍过长");
        }
        teacher.setResumeIntro(intro);
        teacherRepo.save(teacher);
        replaceMedia(teacher.getId(), body.get("photos"), PHOTO, MAX_PHOTOS);
        replaceMedia(teacher.getId(), body.get("videos"), VIDEO, MAX_VIDEOS);
        return toResumeMap(teacher);
    }

    @Transactional
    public void deleteByTeacherId(Long teacherId) {
        mediaRepo.deleteByTeacherId(teacherId);
    }

    public Map<String, Object> summary(Long teacherId) {
        Map<String, Object> map = new LinkedHashMap<>();
        String intro = teacherRepo.findById(teacherId).map(Teacher::getResumeIntro).orElse("");
        long photoCount = mediaRepo.countByTeacherIdAndMediaType(teacherId, PHOTO);
        long videoCount = mediaRepo.countByTeacherIdAndMediaType(teacherId, VIDEO);
        boolean hasIntro = intro != null && !intro.isBlank();
        map.put("hasResume", hasIntro || photoCount > 0 || videoCount > 0);
        map.put("photoCount", photoCount);
        map.put("videoCount", videoCount);
        return map;
    }

    private void replaceMedia(Long teacherId, Object raw, String mediaType, int max) {
        List<String> urls = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                String url;
                if (item instanceof Map<?, ?> map) {
                    url = str(map.get("url"));
                } else {
                    url = str(item);
                }
                if (!url.isBlank()) {
                    urls.add(url);
                }
            }
        }
        if (urls.size() > max) {
            throw new BizException(PHOTO.equals(mediaType)
                    ? "照片最多上传 " + max + " 张"
                    : "视频最多上传 " + max + " 个");
        }
        List<TeacherResumeMedia> existing = mediaRepo.findByTeacherIdOrderBySortOrderAscIdAsc(teacherId);
        for (TeacherResumeMedia item : existing) {
            if (mediaType.equals(item.getMediaType())) {
                mediaRepo.delete(item);
            }
        }
        int index = 0;
        for (String url : urls) {
            TeacherResumeMedia media = new TeacherResumeMedia();
            media.setTeacherId(teacherId);
            media.setMediaType(mediaType);
            media.setUrl(url);
            media.setSortOrder(index++);
            mediaRepo.save(media);
        }
    }

    private Map<String, Object> toResumeMap(Teacher teacher) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teacherId", teacher.getId());
        map.put("teacherName", teacher.getName());
        map.put("resumeIntro", teacher.getResumeIntro() == null ? "" : teacher.getResumeIntro());
        List<Map<String, Object>> photos = new ArrayList<>();
        List<Map<String, Object>> videos = new ArrayList<>();
        for (TeacherResumeMedia media : mediaRepo.findByTeacherIdOrderBySortOrderAscIdAsc(teacher.getId())) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", media.getId());
            row.put("url", media.getUrl());
            row.put("sortOrder", media.getSortOrder());
            if (VIDEO.equals(media.getMediaType())) {
                videos.add(row);
            } else {
                photos.add(row);
            }
        }
        map.put("photos", photos);
        map.put("videos", videos);
        return map;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
