package com.forget.academy.config;

import com.forget.academy.common.CampusIds;
import com.forget.academy.common.CourseModuleTypes;
import com.forget.academy.entity.AdminUser;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.ClassArchive;
import com.forget.academy.entity.Course;
import com.forget.academy.entity.GrowthTrack;
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
import com.forget.academy.repo.ClassArchiveRepo;
import com.forget.academy.repo.CourseRepo;
import com.forget.academy.repo.GrowthTrackRepo;
import com.forget.academy.repo.OpportunityApplyRepo;
import com.forget.academy.repo.OpportunityRepo;
import com.forget.academy.repo.PracticeRecordRepo;
import com.forget.academy.repo.ScheduleRepo;
import com.forget.academy.repo.SchoolRepo;
import com.forget.academy.repo.StudioRepo;
import com.forget.academy.service.CampusCatalogService;
import com.forget.academy.service.CampusContentService;
import com.forget.academy.service.StudioService;
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

import java.time.Instant;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final AdminUserRepo adminUserRepo;
    private final StudioRepo studioRepo;
    private final StudioService studioService;
    private final CampusContentService campusContentService;
    private final BannerRepo bannerRepo;
    private final BrandPhotoRepo brandPhotoRepo;
    private final TeacherRepo teacherRepo;
    private final CourseRepo courseRepo;
    private final GrowthTrackRepo growthTrackRepo;
    private final ScheduleRepo scheduleRepo;
    private final ClassArchiveRepo classArchiveRepo;
    private final OpportunityRepo opportunityRepo;
    private final AppUserRepo appUserRepo;
    private final UserCardRepo userCardRepo;
    private final UserCourseRepo userCourseRepo;
    private final BookingRepo bookingRepo;
    private final PracticeRecordRepo practiceRecordRepo;
    private final OpportunityApplyRepo opportunityApplyRepo;
    private final SchoolRepo schoolRepo;
    private final CampusCatalogService campusCatalogService;
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
        campusCatalogService.migrateLegacyCampusIds();
        seedCourseIntroModules();
        seedCustomerServiceQr();
        seedGrowthTracks();
        removeLegacyProductCourses();
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
            if (studio.getCourseSystemLead() == null || studio.getCourseSystemLead().isBlank()) {
                studio.setCourseSystemLead("按学习方式和目标选择：固定班、次通卡、私教，或定制赛事与商演。");
                changed = true;
            }
            if (studio.getCourseSystemHomeSummary() == null || studio.getCourseSystemHomeSummary().isBlank()) {
                studio.setCourseSystemHomeSummary("特色固定班、次卡、通卡\n私教、定制课、商演赛事");
                changed = true;
            }
            if (studio.getGrowthIntro() == null || studio.getGrowthIntro().isBlank()) {
                studio.setGrowthIntro("FOR-GET不仅是上课，我们把兼职、实习、就业演出、商演、考证等多样化资源嫁接给大家，用舞蹈发展、勤工俭学两条成长线，帮你从学员走向舞台与职场，愿你的大学，因为有FG而更好。");
                changed = true;
            }
            if (studio.getGrowthLevelTip() == null || studio.getGrowthLevelTip().isBlank()) {
                studio.setGrowthLevelTip("新学员默认享有 T1 权益，可通过年限、考核等途径升级至 T2 / T3。");
                changed = true;
            }
            if (studio.getWorkLead() == null || studio.getWorkLead().isBlank()) {
                studio.setWorkLead("勤工俭学成长线：从校园兼职到实习，再到管理角色。点击进入可查看近期机会并报名。");
                changed = true;
            }
            if (studio.getDanceLead() == null || studio.getDanceLead().isBlank()) {
                studio.setDanceLead("舞蹈发展成长线：演出练胆 → 商演实践 → 教师考证与任教。点击进入可查看近期机会并报名。");
                changed = true;
            }
            if (studio.getWorkModuleSummary() == null || studio.getWorkModuleSummary().isBlank()) {
                studio.setWorkModuleSummary("兼职 → 实习 → 管理（T1-T3）");
                changed = true;
            }
            if (studio.getDanceModuleSummary() == null || studio.getDanceModuleSummary().isBlank()) {
                studio.setDanceModuleSummary("演出 → 商演 → 教师（T1-T3）");
                changed = true;
            }
            if (changed) {
                studioRepo.save(studio);
            }
        });
        fillScheduleCampus();
        fillPracticeCampus();
        if (studioRepo.count() == 0) {
            seedStudio();
        }
        studioService.ensureCampusRecords();
        campusContentService.ensureCampusRecords();
        seedClassArchives();
        if (teacherRepo.count() > 0) {
            return;
        }
        seedTeachers();
        seedSchedules();
        seedOpportunities();
    }

    private void seedCustomerServiceQr() {
        courseRepo.findAll().stream()
                .filter(course -> CourseModuleTypes.SYSTEM.equals(course.getModuleType()))
                .filter(course -> course.getActionTab() == null || course.getActionTab().isBlank())
                .filter(course -> course.getCustomerServiceQr() == null || course.getCustomerServiceQr().isBlank())
                .forEach(course -> course.setCustomerServiceQr("/customer-service-qr.jpg"));
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
        studio.setCampusId(CampusIds.DEFAULT);
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
        studio.setCourseSystemLead("按学习方式和目标选择：固定班、次通卡、私教，或定制赛事与商演。");
        studio.setCourseSystemHomeSummary("特色固定班、次卡、通卡\n私教、定制课、商演赛事");
        studio.setGrowthIntro("FOR-GET不仅是上课，我们把兼职、实习、就业演出、商演、考证等多样化资源嫁接给大家，用舞蹈发展、勤工俭学两条成长线，帮你从学员走向舞台与职场，愿你的大学，因为有FG而更好。");
        studio.setGrowthLevelTip("新学员默认享有 T1 权益，可通过年限、考核等途径升级至 T2 / T3。");
        studio.setWorkLead("勤工俭学成长线：从校园兼职到实习，再到管理角色。点击进入可查看近期机会并报名。");
        studio.setDanceLead("舞蹈发展成长线：演出练胆 → 商演实践 → 教师考证与任教。点击进入可查看近期机会并报名。");
        studio.setWorkModuleSummary("兼职 → 实习 → 管理（T1-T3）");
        studio.setDanceModuleSummary("演出 → 商演 → 教师（T1-T3）");
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

    private void removeLegacyProductCourses() {
        courseRepo.findAll().stream()
                .filter(course -> {
                    String type = course.getModuleType();
                    return type == null || type.isBlank() || "product".equals(type);
                })
                .forEach(course -> {
                    userCourseRepo.findAll().stream()
                            .filter(item -> course.getId().equals(item.getCourseId()))
                            .forEach(userCourseRepo::delete);
                    courseRepo.delete(course);
                });
    }

    private void seedCourseIntroModules() {
        if (courseRepo.countByModuleType(CourseModuleTypes.TRIAL) > 0) {
            return;
        }
        Course trial = new Course();
        trial.setModuleType(CourseModuleTypes.TRIAL);
        trial.setName("体验课");
        trial.setPriceDisplay("9.9");
        trial.setPriceUnit("节");
        trial.setTag("新人专享");
        trial.setSummary("一次到店，感受课堂氛围与老师风格");
        trial.setDescription("适合第一次来舞室的同学。用一节体验课了解教室、音乐和上课节奏，再决定适合自己的课程体系。");
        trial.setHighlights("一节团课体验\n到店即可上课\n可咨询老师选课建议");
        trial.setSortOrder(1);
        trial.setEnabled(true);
        courseRepo.save(trial);

        saveSystemModule("fixed", "精品固定班", "固定时段、固定老师，按体系进阶",
                "每周固定上课时间，跟着同一位老师系统训练。适合想长期学、把基础打扎实的同学。",
                "固定班次与教室\n按阶段进阶\n适合持续出勤",
                "查看固定班课表", "fixed", 1);
        saveSystemModule("pass", "次通卡", "按次计费，团课灵活通刷",
                "买次卡后可预约团课，时间更灵活。适合课表不固定、想按自己节奏来上课的同学。",
                "按次扣课\n团课通刷\n约满即来、更自由",
                "咨询购卡", "", 2);
        saveSystemModule("private", "私教", "1 对 1，针对个人问题专项突破",
                "根据你的基础、目标和赛程单独排课。适合想快速提升、准备比赛或需要纠错巩固的同学。",
                "1 对 1 授课\n内容可定制\n时间需与老师协商",
                "预约私教", "private", 3);
        saveSystemModule("custom", "定制课程 · 赛事商演", "编舞定制、比赛集训与商演排练",
                "为社团、比赛、商演或品牌活动定制编舞与排练计划。可按人数、风格和上场时间单独沟通。",
                "编舞定制\n赛事集训\n商演排练",
                "预约咨询", "", 4);
    }

    private void saveSystemModule(String key, String name, String summary, String desc, String highlights,
                                  String actionLabel, String actionTab, int sort) {
        Course course = new Course();
        course.setModuleType(CourseModuleTypes.SYSTEM);
        course.setModuleKey(key);
        course.setName(name);
        course.setSummary(summary);
        course.setDescription(desc);
        course.setHighlights(highlights);
        course.setActionLabel(actionLabel);
        course.setActionTab(actionTab == null || actionTab.isBlank() ? null : actionTab);
        course.setSortOrder(sort);
        course.setEnabled(true);
        courseRepo.save(course);
    }

    private void seedGrowthTracks() {
        if (growthTrackRepo.count() > 0) {
            return;
        }
        saveGrowthTrack("parttime", "work", "勤工俭学", "兼职", "T1",
                "活动执行、课程助理等校园兼职机会", 1);
        saveGrowthTrack("intern", "work", "勤工俭学", "实习", "T2",
                "教务部、招新部、宣传部等正式实习岗位\n链接、内推外界资源的各类可靠岗位", 2);
        saveGrowthTrack("manage", "work", "勤工俭学", "管理", "T3",
                "单项目/分校区/品牌负责人等深度方向", 3);
        saveGrowthTrack("show", "dance", "舞蹈发展", "演出", "T1",
                "校园表演、学期派对、MV拍摄等机会", 1);
        saveGrowthTrack("commercial", "dance", "舞蹈发展", "商演", "T2",
                "FG舞队：商演、品牌邀约和各类赛事", 2);
        saveGrowthTrack("teacher", "dance", "舞蹈发展", "教师", "T3",
                "教师考证（国家级）、带班助教与正式任教", 3);
    }

    private void saveGrowthTrack(String trackKey, String lineKey, String lineName, String name, String level,
                                 String description, int sort) {
        GrowthTrack track = new GrowthTrack();
        track.setCampusId(CampusIds.DEFAULT);
        track.setTrackKey(trackKey);
        track.setLineKey(lineKey);
        track.setLineName(lineName);
        track.setName(name);
        track.setLevel(level);
        track.setDescription(description);
        track.setSortOrder(sort);
        track.setEnabled(true);
        growthTrackRepo.save(track);
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

    /** 插入一条课堂档案示例，便于后台预览续报率等字段 */
    private void seedClassArchives() {
        Schedule schedule = scheduleRepo.findAll().stream()
                .filter(item -> "group".equals(item.getType()) && item.getTeacherId() != null)
                .findFirst()
                .orElse(null);
        if (schedule == null) {
            return;
        }
        String classDate = LocalDate.now().minusDays(3).toString();
        if (classArchiveRepo.findByTeacherIdAndScheduleIdAndClassDate(
                schedule.getTeacherId(), schedule.getId(), classDate).isPresent()) {
            return;
        }
        ClassArchive archive = new ClassArchive();
        archive.setTeacherId(schedule.getTeacherId());
        archive.setScheduleId(schedule.getId());
        archive.setClassDate(classDate);
        archive.setName(schedule.getName());
        archive.setTimeText(schedule.getTimeText());
        archive.setRoom(schedule.getRoom());
        archive.setCampusId(schedule.getCampusId() == null || schedule.getCampusId().isBlank()
                ? CampusIds.DEFAULT : schedule.getCampusId());
        archive.setDuration("75分钟");
        archive.setTeacherCheckedAt(Instant.now());
        archive.setBookedCount(12);
        archive.setCheckedInCount(10);
        archive.setRenewalRate("85%");
        archive.setStudentFeedback("学员整体节奏跟得上，续报意愿较高。");
        archive.setNote("示例数据，便于后台预览课堂档案。");
        classArchiveRepo.save(archive);
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
