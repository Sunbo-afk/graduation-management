package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;

public class StatisticsServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = Auth.getUser(request);
        if (user != null && "student".equals(user.getRoleName()) && user.getRelatedId() != null) {
            request.setAttribute("progress", dao.listProgressByStudent(user.getRelatedId()));
        } else {
            request.setAttribute("progress", dao.listProgress());
        }

        request.setAttribute("teacherGuidance", dao.listTeacherGuidance());
        request.setAttribute("scoreRank", dao.listScoreRank());
        request.setAttribute("logs", dao.listLogs());
        request.getRequestDispatcher("statistics.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!Auth.isAdmin(request) && !Auth.isTeacher(request)) {
            request.setAttribute("message", "只有管理员或教师可以计算总成绩");
            doGet(request, response);
            return;
        }

        String studentId = request.getParameter("studentId");
        String message = dao.calcScore(studentId);
        request.setAttribute("message", message);
        doGet(request, response);
    }
}
