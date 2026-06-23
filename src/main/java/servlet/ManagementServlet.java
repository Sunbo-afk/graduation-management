package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;

public class ManagementServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Auth.isAdmin(request)) {
            request.getRequestDispatcher("forbidden.jsp").forward(request, response);
            return;
        }

        request.setAttribute("students", dao.listStudents());
        request.setAttribute("teachers", dao.listTeachers());
        request.setAttribute("topics", dao.listTopics());
        request.setAttribute("stages", dao.listStages());
        request.setAttribute("deadlines", dao.listDeadlines());
        request.setAttribute("notices", dao.listNotices());
        request.getRequestDispatcher("management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!Auth.isAdmin(request)) {
            request.getRequestDispatcher("forbidden.jsp").forward(request, response);
            return;
        }

        String action = request.getParameter("action");
        if ("assign".equals(action)) {
            String studentId = request.getParameter("studentId");
            String topicId = request.getParameter("topicId");
            String message = dao.selectTopic(studentId, topicId);
            request.setAttribute("message", message);
            doGet(request, response);
            return;
        }

        if ("deadline".equals(action)) {
            String departmentId = request.getParameter("departmentId");
            String stageId = request.getParameter("stageId");
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            boolean ok = dao.setDeadline(departmentId, stageId, startTime, endTime);
            if (ok) {
                response.sendRedirect("management");
            } else {
                request.setAttribute("message", "设置期限失败");
                doGet(request, response);
            }
            return;
        }

        if ("notice".equals(action)) {
            User user = Auth.getUser(request);
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            boolean ok = dao.addNotice(user.getUserId(), title, content);
            if (ok) {
                response.sendRedirect("management");
            } else {
                request.setAttribute("message", "发布公告失败");
                doGet(request, response);
            }
        }
    }
}
