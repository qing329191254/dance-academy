package com.forget.academy.config;

import com.forget.academy.common.CampusIds;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.Course;
import com.forget.academy.entity.Opportunity;
import com.forget.academy.entity.Schedule;
import com.forget.academy.entity.School;
import com.forget.academy.entity.Studio;
import com.forget.academy.entity.Teacher;
import com.forget.academy.entity.UserCard;
import com.forget.academy.repo.AdminUserRepo;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.BannerRepo;
import com.forget.academy.repo.BookingRepo;
import com.forget.academy.repo.BrandPhotoRepo;
import com.forget.academy.repo.CourseRepo;
import com.forget.academy.repo.OpportunityApplyRepo;
import com.forget.academy.repo.OpportunityRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.SchoolRepo;
import com.forget.academy.repo.StudioRepo;
import com.forget.academy.repo.TeacherRepo;
import com.forget.academy.repo.UserCardRepo;
import com.forget.academy.repo.UserCourseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final AdminUserRepo adminUserRepo;
    private final StudioRepo studioRepo;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;
    private final TeacherRepo teacherRepo;
    private final CourseRepo courseRepo;
    private final ScheduleRepo scheduleRepo;
    private final OpportunityRepo opportunityRepo;
    private final AppUserRepo appUserRepo;
    private final UserCardRepo userCardRepo;
    private final UserCourseRepo userCourseRepo;
    private final BookingRepo bookingRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final OpportunityApplyRepo opportunityApplyRepo;
    private final SchoolRepo schoolRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.admin-username}")
    private String adminUsername;
    @Value("${app.admin-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedSchools();
        removeDemoUsers();
        clearPackagedMedia();
        studioRepo.findAll().stream().findFirst().ifPresent(studio -> {
            boolean changed = false;
            if ("高校FOR一GET街舞俱乐部".equals(studio.getName())) {
                studio.setName("高校FOR-GET舞室");
                changed = true;
            }
            if ("四川成都 · 高校街舞俱乐部".equals(studio.getLocation())) {
                studio.setLocation("四川成都 · 高校FOR-GET舞室");
                changed = true;
            }
            if (studio.getLogo() == null || studio.getLogo().isBlank() || studio.getLogo().startsWith("/uploads/")) {
                studio.setLogo("/logo.png");
                changed = true;
            }
            if (changed) {
                studioRepo.save(studio);
            }
        });
        fillScheduleCampus();
        fillPracticeCampus();
        if (studioRepo.count() > 0) {
            return;
        }
        seedStudio();
        seedTeachers();
        seedCourses();
        seedSchedules();
        seedOpportunities();
    }

    private void clearPackagedMedia() {
        bannerRepo.findAll().stream()
                .filter(item -> isPackagedMedia(item.getImageUrl()))
                .forEach(bannerRepo::delete);
        brandPhotoRepo.findAll().stream()
                .filter(item -> isPackagedMedia(item.getImageUrl()))
                .forEach(brandPhotoRepo::delete);
        teacherRepo.findAll().forEach(teacher -> {
            if (isPackagedMedia(teacher.getAvatar())) {
                teacher.setAvatar(null);
                teacherRepo.save(teacher);
            }
        });
        courseRepo.findAll().forEach(course -> {
            if (isPackagedMedia(course.getCover())) {
                course.setCover(null);
                courseRepo.save(course);
            }
        });
        userCardRepo.findAll().forEach(card -> {
            if (isPackagedMedia(card.getCover())) {
                card.setCover(null);
                userCardRepo.save(card);
            }
        });
        studioRepo.findAll().forEach(studio -> {
            if (isPackagedMedia(studio.getSplashImage())) {
                studio.setSplashImage(null);
                studioRepo.save(studio);
            }
        });
    }

    private boolean isPackagedMedia(String url) {
        return url != null && (url.startsWith("/static/") || url.startsWith("static/"));
    }

    private void removeDemoUsers() {
        appUserRepo.findAll().stream()
                .filter(user -> isFakeOpenid(user.getOpenid()))
                .forEach(this::purgeUser);
    }

    private boolean isFakeOpenid(String openid) {
        if (openid == null || openid.isBlank()) {
            return false;
        }
        return openid.startsWith("dev_") || "demo_student".equals(openid) || "dev_weixin".equals(openid);
    }

    private void purgeUser(AppUser user) {
        Long id = user.getId();
        userCardRepo.deleteByUserId(id);
        userCourseRepo.deleteByUserId(id);
        bookingRepo.deleteByUserId(id);
        practiceRecordRepo.deleteByUserId(id);
        opportunityApplyRepo.deleteByUserId(id);
        appUserRepo.delete(user);
    }

    private void seedAdmin() {
        adminUserRepo.findByUsername(adminUsername).ifPresentOrElse(admin -> {
            if ("ADMIN".equals(admin.getRole())) {
                admin.setRole(com.forget.academy.common.AdminRoles.SUPER_ADMIN);
                adminUserRepo.save(admin);
            }
        }, () -> {
            AdminUser admin = new AdminUser();
            admin.setUsername(adminUsername);
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setName("超级管理员");
            admin.setRole(com.forget.academy.common.AdminRoles.SUPER_ADMIN);
            adminUserRepo.save(admin);
        });
    }

    private void seedSchools() {
        if (schoolRepo.count() > 0) {
            return;
        }
        String[] names = {
                "四川师范大学",
                "北京师范大学（珠海）",
                "北师香港浸会大学",
                "成都大学",
                "西南石油大学",
                "四川大学",
                "电子科技大学",
                "西南交通大学",
                "其他"
        };
        for (int i = 0; i < names.length; i++) {
            School school = new School();
            school.setName(names[i]);
            school.setSortOrder(i + 1);
            school.setEnabled(true);
            schoolRepo.save(school);
        }
    }

    private void seedStudio() {
        Studio studio = new Studio();
        studio.setName("高校FOR-GET舞室");
        studio.setLocation("四川成都 · 高校FOR-GET舞室");
        studio.setCity("四川成都");
        studio.setAddress("四川省成都市");
        studio.setLatitude(30.659462);
        studio.setLongitude(104.065735);
        studio.setBusinessHours("营业时间 13:00-22:00");
        studio.setPhone("02888881234");
        studio.setPhoneDisplay("028-8888-1234");
        studio.setLogo("/logo.png");
        studio.setIntro("深耕高校街舞文化的俱乐部品牌。课堂之外，用勤工俭学与舞蹈发展双线赋能大学生成长，增强机构黏性。");
        studio.setBusiness("团课 / 固定班 / 私教课 / 成长中心");
        studio.setSlogan("DANCE UP · BREAK FREE");
        studioRepo.save(studio);
    }

    private void seedTeachers() {
        saveTeacher("金大铭", "HipHop", "校队主力，擅长编舞与舞台表现", "", 1);
        saveTeacher("龙龙", "Jazz", "Jazz 体系主教，课程节奏感强", "", 2);
        saveTeacher("90", "Breaking", "Breaking 专项，带队比赛经验丰富", "", 3);
        saveTeacher("小朱", "Waacking", "Waacking / 女团风，舞台感突出", "", 4);
    }

    private void saveTeacher(String name, String style, String intro, String avatar, int sort) {
        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setStyle(style);
        teacher.setIntro(intro);
        teacher.setAvatar(avatar);
        teacher.setSortOrder(sort);
        teacher.setEnabled(true);
        teacherRepo.save(teacher);
    }

    private void seedCourses() {
        saveCourse("HipHop 入门", 199, "零基础", "节奏、律动与基础脚步", 1);
        saveCourse("Jazz 二星课", 299, "进阶", "组合编排与表现力训练", 2);
        saveCourse("Breaking 专项", 399, "进阶", "Footwork / Freeze / Power", 3);
    }

    private void saveCourse(String name, int price, String level, String desc, int sort) {
        Course course = new Course();
        course.setName(name);
        course.setPrice(price);
        course.setLevel(level);
        course.setDescription(desc);
        course.setSortOrder(sort);
        course.setEnabled(true);
        courseRepo.save(course);
    }

    private void seedSchedules() {
        Teacher jin = teacherRepo.findAll().stream().filter(t -> "金大铭".equals(t.getName())).findFirst().orElse(null);
        Teacher longlong = teacherRepo.findAll().stream().filter(t -> "龙龙".equals(t.getName())).findFirst().orElse(null);
        Teacher ninety = teacherRepo.findAll().stream().filter(t -> "90".equals(t.getName())).findFirst().orElse(null);
        Teacher zhu = teacherRepo.findAll().stream().filter(t -> "小朱".equals(t.getName())).findFirst().orElse(null);

        saveSchedule("group", "HIPHOP", "16:00-17:15", jin, "二楼 Room B", 3, "可预约", 1, 20, 1);
        saveSchedule("group", "JAZZ二星课", "18:00-19:15", longlong, "二楼 Room A", 4, "可预约", 1, 16, 2);
        saveSchedule("group", "Breaking 基础", "14:00-15:30", ninety, "一楼 Studio", 3, "可预约", 2, 16, 3);
        saveSchedule("group", "Waacking", "19:00-20:15", zhu, "二楼 Room B", 4, "可预约", 2, 16, 4);
        saveSchedule("group", "HIPHOP", "16:00-17:15", jin, "二楼 Room B", 3, "可预约", 3, 20, 5);
        saveSchedule("group", "JAZZ二星课", "18:00-19:15", longlong, "二楼 Room A", 4, "名额紧张", 4, 12, 6);
        saveSchedule("group", "Breaking 专项", "15:00-16:30", ninety, "一楼 Studio", 4, "可预约", 6, 16, 7);

        saveSchedule("fixed", "周末固定班 · HipHop", "周六 14:00-15:30", ninety, "一楼 Studio", 3, "招生中", 6, 20, 1);
        saveSchedule("fixed", "周中固定班 · Jazz", "周三 19:30-21:00", longlong, "二楼 Room A", 4, "名额紧张", 3, 12, 2);

        saveSchedule("private", "1v1 私教 · 编舞", "预约制", jin, "私教室", 5, "可预约", null, 1, 1);
        saveSchedule("private", "1v1 私教 · 基础巩固", "预约制", zhu, "私教室", 4, "可预约", null, 1, 2);
    }

    private void saveSchedule(String type, String name, String time, Teacher teacher, String room,
                              int stars, String status, Integer weekday, int capacity, int sort) {
        Schedule item = new Schedule();
        item.setType(type);
        item.setName(name);
        item.setTimeText(time);
        if (teacher != null) {
            item.setTeacherId(teacher.getId());
            item.setTeacherName(teacher.getName());
        }
        item.setRoom(room);
        item.setStars(stars);
        item.setStatus(status);
        item.setWeekday(weekday);
        item.setCapacity(capacity);
        item.setSortOrder(sort);
        item.setCampusId(CampusIds.DEFAULT);
        item.setEnabled(true);
        scheduleRepo.save(item);
    }

    private void fillPracticeCampus() {
        practiceRecordRepo.findAll().forEach(record -> {
            if (record.getCampusId() != null && !record.getCampusId().isBlank()) {
                return;
            }
            String campus = CampusIds.DEFAULT;
            try {
                Long scheduleId = Long.parseLong(record.getSessionId());
                campus = scheduleRepo.findById(scheduleId)
                        .map(Schedule::getCampusId)
                        .filter(id -> id != null && !id.isBlank())
                        .orElse(CampusIds.DEFAULT);
            } catch (Exception ignored) {
                // keep default
            }
            record.setCampusId(campus);
            practiceRecordRepo.save(record);
        });
    }

    private void fillScheduleCampus() {
        scheduleRepo.findAll().forEach(schedule -> {
            if (schedule.getCampusId() == null || schedule.getCampusId().isBlank()) {
                schedule.setCampusId(CampusIds.DEFAULT);
                scheduleRepo.save(schedule);
            }
        });
    }

    private void seedOpportunities() {
        saveOpp("parttime", "w1", "周末活动执行助理", LocalDate.of(2026, 9, 5), 6, "T1", "协助活动布场与现场执行，适合新学员积累经验。");
        saveOpp("parttime", "w2", "公开课现场助教", LocalDate.of(2026, 9, 12), 4, "T1", "协助老师控场、签到与学员引导。");
        saveOpp("intern", "w3", "品牌内容运营实习", LocalDate.of(2026, 9, 20), 2, "T2", "短视频选题、拍摄协助与社群内容更新。");
        saveOpp("manage", "w4", "秋季项目组负责人选拔", LocalDate.of(2026, 10, 1), 1, "T3", "负责一组学员活动统筹，需具备 T3 管理权益。");
        saveOpp("show", "d1", "迎新晚会节目海选", LocalDate.of(2026, 9, 8), 12, "T1", "HipHop / Jazz 小组节目，通过后进入排练。");
        saveOpp("commercial", "d2", "商场品牌快闪商演", LocalDate.of(2026, 9, 18), 8, "T2", "商演排练 2 次 + 正式演出 1 场。");
        saveOpp("teacher", "d3", "年度教师资格考证班", LocalDate.of(2026, 10, 15), 10, "T3", "面向达到教师成长线 T3 的学员开放报名。");
    }

    private void saveOpp(String track, String code, String title, LocalDate deadline, int spots, String level, String summary) {
        Opportunity item = new Opportunity();
        item.setTrackKey(track);
        item.setCode(code);
        item.setTitle(title);
        item.setDeadline(deadline);
        item.setSpots(spots);
        item.setLevel(level);
        item.setSummary(summary);
        item.setEnabled(true);
        opportunityRepo.save(item);
    }
}
