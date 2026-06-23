package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;

public class NoticeServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("notices", dao.listNotices());
        request.getRequestDispatcher("notices.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        String title = request.getParameter("title");
        String content = request.getParameter("content");

        boolean ok = false;
        if (user != null && "admin".equals(user.getRoleName())) {
            ok = dao.addNotice(user.getUserId(), title, content);
        }

        if (ok) {
            response.sendRedirect("notices");
        } else {
            request.setAttribute("message", "只有管理员可以发布公告");
            doGet(request, response);
        }
    }
}
