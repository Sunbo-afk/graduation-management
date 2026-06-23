package com.gms.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Whitelist
        if (uri.equals("/login") || uri.equals("/register") || uri.startsWith("/css/") || uri.startsWith("/js/")
            || uri.startsWith("/images/") || uri.startsWith("/fonts/")) {
            return true;
        }
        // Allow static resources
        if (uri.contains(".") && !uri.endsWith(".html")) {
            return true;
        }

        HttpSession session = request.getSession();
        Object user = session.getAttribute("loginUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
