package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.io.IOException;

public class ReviewServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Auth.isTeacher(request) && !Auth.isAdmin(request)) {
            request.getRequestDispatcher("forbidden.jsp").forward(request, response);
            return;
        }

        request.setAttribute("reviews", dao.listReviews());
        request.setAttribute("submissions", dao.listSubmissions());
        request.setAttribute("teachers", dao.listTeachers());
        request.getRequestDispatcher("reviews.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!Auth.isTeacher(request)) {
            request.setAttribute("message", "只有教师可以提交评阅");
            doGet(request, response);
            return;
        }

        User user = Auth.getUser(request);
        String submissionId = request.getParameter("submissionId");
        String teacherId = request.getParameter("teacherId");
        if (user.getRelatedId() != null) {
            teacherId = String.valueOf(user.getRelatedId());
        }

        String comment = request.getParameter("comment");
        String score = request.getParameter("score");
        boolean ok = dao.addReview(submissionId, teacherId, comment, score);

        if (ok) {
            response.sendRedirect("reviews");
        } else {
            request.setAttribute("message", "评阅失败，同一提交只能评阅一次");
            doGet(request, response);
        }
    }
}
