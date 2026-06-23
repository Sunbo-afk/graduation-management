package com.gms.controller;

import com.gms.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId, @RequestParam String password,
                        @RequestParam String role, HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Map<String, Object> result = loginService.authenticate(userId, password, role);
        if ((Boolean) result.get("success")) {
            session.setAttribute("loginUser", result);
            String roleStr = (String) result.get("role");
            switch (roleStr) {
                case "ADMIN":
                    return "redirect:/admin/index";
                case "TEACHER":
                    return "redirect:/teacher/index";
                case "STUDENT":
                    return "redirect:/student/index";
                default:
                    return "redirect:/login";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", result.get("message"));
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
