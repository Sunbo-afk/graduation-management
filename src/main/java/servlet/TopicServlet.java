package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;

public class TopicServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("topics", dao.listTopics());
        request.setAttribute("teachers", dao.listTeachers());
        request.getRequestDispatcher("topics.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("select".equals(action)) {
            if (!Auth.isStudent(request)) {
                request.setAttribute("message", "只有学生可以执行选题");
                doGet(request, response);
                return;
            }

            User user = Auth.getUser(request);
            String studentId = request.getParameter("studentId");
            if (user.getRelatedId() != null) {
                studentId = String.valueOf(user.getRelatedId());
            }

            String topicId = request.getParameter("topicId");
            String message = dao.selectTopic(studentId, topicId);
            request.setAttribute("message", message);
            doGet(request, response);
        } else {
            if (!Auth.isTeacher(request) && !Auth.isAdmin(request)) {
                request.setAttribute("message", "只有教师或管理员可以新增题目");
                doGet(request, response);
                return;
            }

            User user = Auth.getUser(request);
            String teacherId = request.getParameter("teacherId");
            if (Auth.isTeacher(request) && user.getRelatedId() != null) {
                teacherId = String.valueOf(user.getRelatedId());
            }

            String title = request.getParameter("title");
            String desc = request.getParameter("desc");
            String skill = request.getParameter("skill");
            String maxStudents = request.getParameter("maxStudents");
            boolean ok = dao.addTopic(teacherId, title, desc, skill, maxStudents);
            if (ok) {
                response.sendRedirect("topics");
            } else {
                request.setAttribute("message", "新增题目失败");
                doGet(request, response);
            }
        }
    }
}
