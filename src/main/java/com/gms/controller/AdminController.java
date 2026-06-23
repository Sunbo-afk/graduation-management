package com.gms.controller;

import com.gms.entity.*;
import com.gms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private DepartmentService departmentService;
    @Autowired private MajorService majorService;
    @Autowired private ClassInfoService classInfoService;
    @Autowired private StudentService studentService;
    @Autowired private TeacherService teacherService;
    @Autowired private TopicService topicService;
    @Autowired private SelectionService selectionService;
    @Autowired private DeadlineService deadlineService;
    @Autowired private SysUserService sysUserService;
    @Autowired private StatisticsService statisticsService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    private boolean isAdmin(HttpSession session) {
        Map<String, Object> user = (Map<String, Object>) session.getAttribute("loginUser");
        return user != null && "ADMIN".equals(user.get("role"));
    }

    // ==================== Dashboard ====================
    @GetMapping("/index")
    public String index(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        Map<String, Object> dashboardStats = statisticsService.dashboardStats();
        model.addAttribute("stats", dashboardStats);
        return "admin/index";
    }

    // ==================== Departments ====================
    @GetMapping("/departments")
    public String departments(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        List<Department> departments = departmentService.list();
        model.addAttribute("departments", departments);
        return "admin/departments";
    }

    @PostMapping("/department/save")
    public String saveDepartment(@ModelAttribute Department department, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            if (department.getDeptId() != null) {
                departmentService.updateById(department);
                redirectAttributes.addFlashAttribute("success", "院系信息更新成功");
            } else {
                departmentService.save(department);
                redirectAttributes.addFlashAttribute("success", "院系添加成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/department/delete/{id}")
    public String deleteDepartment(@PathVariable Integer id, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            departmentService.removeById(id);
            redirectAttributes.addFlashAttribute("success", "院系删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/departments";
    }

    // ==================== Majors ====================
    @GetMapping("/majors")
    public String majors(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        List<Major> majors = majorService.list();
        List<Department> departments = departmentService.list();
        Map<Integer, String> deptMap = new HashMap<>();
        for (Department d : departments) {
            deptMap.put(d.getDeptId(), d.getDeptName());
        }
        model.addAttribute("majors", majors);
        model.addAttribute("departments", departments);
        model.addAttribute("deptMap", deptMap);
        return "admin/majors";
    }

    @PostMapping("/major/save")
    public String saveMajor(@ModelAttribute Major major, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            if (major.getMajorId() != null) {
                majorService.updateById(major);
                redirectAttributes.addFlashAttribute("success", "专业信息更新成功");
            } else {
                majorService.save(major);
                redirectAttributes.addFlashAttribute("success", "专业添加成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/majors";
    }

    @GetMapping("/major/delete/{id}")
    public String deleteMajor(@PathVariable Integer id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            majorService.removeById(id);
            redirectAttributes.addFlashAttribute("success", "专业删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/majors";
    }

    // ==================== Classes ====================
    @GetMapping("/classes")
    public String classes(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/login";
        List<ClassInfo> classes = classInfoService.list();
        List<Major> majors = majorService.list();
        Map<Integer, String> majorMap = new HashMap<>();
        for (Major m : majors) {
            majorMap.put(m.getMajorId(), m.getMajorName());
        }
        model.addAttribute("classes", classes);
        model.addAttribute("majors", majors);
        model.addAttribute("majorMap", majorMap);
        return "admin/classes";
    }

    @PostMapping("/class/save")
    public String saveClass(@ModelAttribute ClassInfo classInfo, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            if (classInfo.getClassId() != null) {
                classInfoService.updateById(classInfo);
                redirectAttributes.addFlashAttribute("success", "班级信息更新成功");
            } else {
                classInfoService.save(classInfo);
                redirectAttributes.addFlashAttribute("success", "班级添加成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/classes";
    }

    @GetMapping("/class/delete/{id}")
    public String deleteClass(@PathVariable Integer id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            classInfoService.removeById(id);
            redirectAttributes.addFlashAttribute("success", "班级删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/classes";
    }

    // ==================== Deadlines ====================
    @GetMapping("/deadlines")
    public String deadlines(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        List<Deadline> deadlines = deadlineService.list();
        model.addAttribute("deadlines", deadlines);
        return "admin/deadlines";
    }

    @PostMapping("/deadline/save")
    public String saveDeadline(@ModelAttribute Deadline deadline, HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            if (deadline.getDeadlineId() != null) {
                deadlineService.updateById(deadline);
                redirectAttributes.addFlashAttribute("success", "截止日期更新成功");
            } else {
                deadlineService.save(deadline);
                redirectAttributes.addFlashAttribute("success", "截止日期添加成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/deadlines";
    }

    @GetMapping("/deadline/delete/{id}")
    public String deleteDeadline(@PathVariable Integer id, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            deadlineService.removeById(id);
            redirectAttributes.addFlashAttribute("success", "截止日期删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/deadlines";
    }

    // ==================== Users ====================
    @GetMapping("/users")
    public String users(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        List<Student> students = studentService.list();
        List<Teacher> teachers = teacherService.list();
        List<Department> departments = departmentService.list();
        // Build department id -> name map
        Map<Integer, String> deptMap = new java.util.HashMap<>();
        for (Department d : departments) {
            deptMap.put(d.getDeptId(), d.getDeptName());
        }
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        model.addAttribute("deptMap", deptMap);
        return "admin/users";
    }

    @PostMapping("/user/reset-password")
    public String resetPassword(@RequestParam String type, @RequestParam String userId,
                                RedirectAttributes redirectAttributes) {
        String encodedPwd = passwordEncoder.encode("123456");
        if ("student".equals(type)) {
            Student student = studentService.getById(userId);
            if (student != null) {
                student.setPassword(encodedPwd);
                studentService.updateById(student);
                redirectAttributes.addFlashAttribute("success", "密码已重置为123456");
            }
        } else if ("teacher".equals(type)) {
            Teacher teacher = teacherService.getById(userId);
            if (teacher != null) {
                teacher.setPassword(encodedPwd);
                teacherService.updateById(teacher);
                redirectAttributes.addFlashAttribute("success", "密码已重置为123456");
            }
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/user/delete-student/{stuId}")
    public String deleteStudent(@PathVariable String stuId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            // 先删选题（如有），再删学生
            selectionService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Selection>()
                .eq(Selection::getStuId, stuId));
            studentService.removeById(stuId);
            redirectAttributes.addFlashAttribute("success", "学生 " + stuId + " 已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/user/delete-teacher/{teacherId}")
    public String deleteTeacher(@PathVariable String teacherId, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        try {
            // 先删该教师的题目和选题，再删教师
            topicService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                .eq(Topic::getTeacherId, teacherId));
            teacherService.removeById(teacherId);
            redirectAttributes.addFlashAttribute("success", "教师 " + teacherId + " 已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==================== Statistics ====================
    @GetMapping("/statistics")
    public String statistics(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        Map<String, Object> dashboardStats = statisticsService.dashboardStats();
        model.addAttribute("stats", dashboardStats);
        model.addAttribute("scoreRankings", statisticsService.scoreRankingByDept(null));
        model.addAttribute("teacherStats", statisticsService.teacherStudentCount());
        Map<String, Object> deptStatsWrapper = statisticsService.deptCompletionStats(null);
        model.addAttribute("deptStats", deptStatsWrapper.get("deptStats"));
        return "admin/statistics";
    }

    // ==================== Backup ====================
    @GetMapping("/backup")
    public String backup(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "admin/backup";
    }
}
