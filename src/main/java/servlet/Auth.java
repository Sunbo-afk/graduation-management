package servlet;

import jakarta.servlet.http.HttpServletRequest;
import model.User;

public class Auth {
    public static User getUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User user = getUser(request);
        if (user != null && "admin".equals(user.getRoleName())) {
            return true;
        }
        return false;
    }

    public static boolean isTeacher(HttpServletRequest request) {
        User user = getUser(request);
        if (user != null && "teacher".equals(user.getRoleName())) {
            return true;
        }
        return false;
    }

    public static boolean isStudent(HttpServletRequest request) {
        User user = getUser(request);
        if (user != null && "student".equals(user.getRoleName())) {
            return true;
        }
        return false;
    }
}
