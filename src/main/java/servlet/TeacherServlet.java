package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class TeacherServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Auth.isAdmin(request)) {
            request.getRequestDispatcher("forbidden.jsp").forward(request, response);
            return;
        }

        request.setAttribute("teachers", dao.listTeachers());
        request.getRequestDispatcher("teachers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!Auth.isAdmin(request)) {
            request.setAttribute("message", "只有管理员可以维护教师");
            doGet(request, response);
            return;
        }

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            String teacherId = request.getParameter("teacherId");
            boolean ok = dao.deleteTeacher(teacherId);
            if (ok) {
                response.sendRedirect("teachers");
            } else {
                request.setAttribute("message", "删除教师失败，该教师可能已有课题或评阅记录");
                doGet(request, response);
            }
            return;
        }

        String departmentId = request.getParameter("departmentId");
        String teacherNo = request.getParameter("teacherNo");
        String teacherName = request.getParameter("teacherName");
        String direction = request.getParameter("direction");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");

        boolean ok = dao.addTeacher(departmentId, teacherNo, teacherName, direction, phone, email);
        if (ok) {
            response.sendRedirect("teachers");
        } else {
            request.setAttribute("message", "新增教师失败，请检查工号或账号是否重复");
            doGet(request, response);
        }
    }
}
