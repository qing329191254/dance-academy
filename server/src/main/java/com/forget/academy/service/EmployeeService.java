package com.forget.academy.service;

import com.forget.academy.common.AppRoles;
import com.forget.academy.common.BizException;
import com.forget.academy.entity.AppUser;
import com.forget.academy.entity.EmployeePerformance;
import com.forget.academy.entity.EmployeeProfile;
import com.forget.academy.entity.EmployeeWeeklyReport;
import com.forget.academy.repo.AppUserRepo;
import com.forget.academy.repo.EmployeePerformanceRepo;
import com.forget.academy.repo.EmployeeProfileRepo;
import com.forget.academy.repo.EmployeeWeeklyReportRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final AppUserRepo appUserRepo;
    private final EmployeeProfileRepo employeeProfileRepo;
    private final EmployeeWeeklyReportRepo weeklyReportRepo;
    private final EmployeePerformanceRepo performanceRepo;

    public AppUser requireEmployee(Long userId) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        if (!AppRoles.EMPLOYEE.equalsIgnoreCase(user.getRole())) {
            throw new BizException("当前账号不是员工");
        }
        if (user.getCampusId() == null || user.getCampusId().isBlank()) {
            throw new BizException("员工账号未绑定校区");
        }
        return user;
    }

    public Map<String, Object> profile(Long userId) {
        AppUser user = requireEmployee(userId);
        EmployeeProfile profile = employeeProfileRepo.findByUserId(userId).orElseGet(EmployeeProfile::new);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("campusId", user.getCampusId());
        map.put("jobTitle", profile.getJobTitle());
        map.put("jobDescription", profile.getJobDescription());
        map.put("nickname", user.getNickname());
        return map;
    }

    @Transactional
    public EmployeeProfile saveProfile(Long userId, String campusId, String jobTitle, String jobDescription) {
        AppUser user = appUserRepo.findById(userId).orElseThrow(() -> new BizException("用户不存在"));
        if (campusId != null && !campusId.isBlank()) {
            user.setCampusId(campusId.trim());
            appUserRepo.save(user);
        }
        EmployeeProfile profile = employeeProfileRepo.findByUserId(userId).orElseGet(() -> {
            EmployeeProfile item = new EmployeeProfile();
            item.setUserId(userId);
            return item;
        });
        profile.setCampusId(user.getCampusId());
        if (jobTitle != null) {
            profile.setJobTitle(jobTitle.trim());
        }
        if (jobDescription != null) {
            profile.setJobDescription(jobDescription.trim());
        }
        return employeeProfileRepo.save(profile);
    }

    public List<EmployeeWeeklyReport> myReports(Long userId) {
        requireEmployee(userId);
        return weeklyReportRepo.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    @Transactional
    public EmployeeWeeklyReport submitReport(Long userId, String weekLabel, String content) {
        requireEmployee(userId);
        if (content == null || content.trim().length() < 5) {
            throw new BizException("请至少填写 5 个字的周报内容");
        }
        EmployeeWeeklyReport report = new EmployeeWeeklyReport();
        report.setUserId(userId);
        report.setWeekLabel(weekLabel == null || weekLabel.isBlank() ? defaultWeekLabel() : weekLabel.trim());
        report.setContent(content.trim());
        report.setSubmittedAt(Instant.now());
        return weeklyReportRepo.save(report);
    }

    public List<EmployeePerformance> myPerformance(Long userId) {
        requireEmployee(userId);
        return performanceRepo.findByUserIdOrderByPublishedAtDesc(userId);
    }

    @Transactional
    public EmployeePerformance publishPerformance(Long userId, String periodLabel, String content) {
        if (content == null || content.isBlank()) {
            throw new BizException("请填写工作成绩内容");
        }
        EmployeePerformance item = new EmployeePerformance();
        item.setUserId(userId);
        item.setPeriodLabel(periodLabel == null ? "" : periodLabel.trim());
        item.setContent(content.trim());
        item.setPublishedAt(Instant.now());
        return performanceRepo.save(item);
    }

    private String defaultWeekLabel() {
        return java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai")).toString();
    }
}
