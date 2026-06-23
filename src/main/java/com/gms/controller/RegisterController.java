package com.gms.controller;

import com.gms.entity.*;
import com.gms.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RegisterController {

    @Autowired private StudentService studentService;
    @Autowired private TeacherService teacherService;
    @Autowired private DepartmentService departmentService;
    @Autowired private MajorService majorService;
    @Autowired private ClassInfoService classInfoService;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerPage(Model model) {
        List<Department> departments = departmentService.list();
        List<Major> majors = majorService.list();
        List<ClassInfo> classes = classInfoService.list();
        model.addAttribute("departments", departments);
        model.addAttribute("majors", majors);
        model.addAttribute("classes", classes);
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String role,
                             @RequestParam String userId,
                             @RequestParam String userName,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) Integer classId,
                             @RequestParam(required = false) Integer deptId,
                             @RequestParam(required = false) String title,
                             @RequestParam(required = false) String researchDirection,
                             RedirectAttributes redirectAttributes) {

        // 1. Validate passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "两次输入的密码不一致");
            return "redirect:/register";
        }

        // 2. Validate password length
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "密码长度不能少于6位");
            return "redirect:/register";
        }

        String encodedPwd = passwordEncoder.encode(password);

        if ("STUDENT".equals(role)) {
            // Validate student fields
            if (classId == null) {
                redirectAttributes.addFlashAttribute("error", "请选择班级");
                return "redirect:/register";
            }
            // Check duplicate stuId
            if (studentService.getById(userId) != null) {
                redirectAttributes.addFlashAttribute("error", "该学号已被注册");
                return "redirect:/register";
            }
            Student student = new Student();
            student.setStuId(userId);
            student.setStuName(userName);
            student.setPassword(encodedPwd);
            student.setClassId(classId);
            student.setPhone(phone);
            student.setEmail(email);
            studentService.save(student);

        } else if ("TEACHER".equals(role)) {
            // Validate teacher fields
            if (deptId == null) {
                redirectAttributes.addFlashAttribute("error", "请选择所属院系");
                return "redirect:/register";
            }
            // Check duplicate teacherId
            if (teacherService.getById(userId) != null) {
                redirectAttributes.addFlashAttribute("error", "该工号已被注册");
                return "redirect:/register";
            }
            Teacher teacher = new Teacher();
            teacher.setTeacherId(userId);
            teacher.setTeacherName(userName);
            teacher.setPassword(encodedPwd);
            teacher.setDeptId(deptId);
            teacher.setTitle(title);
            teacher.setResearchDirection(researchDirection);
            teacher.setPhone(phone);
            teacher.setEmail(email);
            teacher.setMaxStudents(5); // default
            teacherService.save(teacher);

        } else {
            redirectAttributes.addFlashAttribute("error", "无效的角色类型");
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
        return "redirect:/login";
    }
}
