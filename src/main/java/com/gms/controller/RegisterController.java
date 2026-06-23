package com.gms.controller;

import com.gms.entity.*;
import com.gms.service.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RegisterController {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final DepartmentService departmentService;
    private final MajorService majorService;
    private final ClassInfoService classInfoService;
    private final BCryptPasswordEncoder passwordEncoder;

    public RegisterController(StudentService studentService,
                              TeacherService teacherService,
                              DepartmentService departmentService,
                              MajorService majorService,
                              ClassInfoService classInfoService,
                              BCryptPasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.departmentService = departmentService;
        this.majorService = majorService;
        this.classInfoService = classInfoService;
        this.passwordEncoder = passwordEncoder;
    }

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

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "两次输入的密码不一致");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "密码长度不能少于6位");
            return "redirect:/register";
        }

        String encodedPwd = passwordEncoder.encode(password);

        if ("STUDENT".equals(role)) {
            if (classId == null) {
                redirectAttributes.addFlashAttribute("error", "请选择班级");
                return "redirect:/register";
            }
            if (studentService.getById(userId) != null) {
                redirectAttributes.addFlashAttribute("error", "该学号已被注册");
                return "redirect:/register";
            }
            Student student = new Student();
            student.setStuId(userId);
            student.setStuName(userName);
            student.setPassword(encodedPwd);
            student.setClassId(classId);
            student.setPhone(blankToNull(phone));
            student.setEmail(blankToNull(email));
            studentService.save(student);

        } else if ("TEACHER".equals(role)) {
            if (deptId == null) {
                redirectAttributes.addFlashAttribute("error", "请选择所属院系");
                return "redirect:/register";
            }
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
            teacher.setPhone(blankToNull(phone));
            teacher.setEmail(blankToNull(email));
            teacher.setMaxStudents(5);
            teacherService.save(teacher);

        } else {
            redirectAttributes.addFlashAttribute("error", "无效的角色类型");
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("success", "注册成功，请登录");
        return "redirect:/login";
    }

    /** 空字符串转 null，避免 CHECK 约束报错 */
    private String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s;
    }
}
