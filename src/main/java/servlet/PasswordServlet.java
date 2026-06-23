package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;

public class PasswordServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = Auth.getUser(request);
        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (newPassword == null || newPassword.length() == 0) {
            request.setAttribute("message", "新密码不能为空");
            doGet(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("message", "两次输入的新密码不一致");
            doGet(request, response);
            return;
        }

        boolean ok = dao.changePassword(user.getUserId(), oldPassword, newPassword);
        if (ok) {
            request.setAttribute("message", "密码修改成功");
        } else {
            request.setAttribute("message", "密码修改失败，请检查原密码");
        }
        doGet(request, response);
    }
}
