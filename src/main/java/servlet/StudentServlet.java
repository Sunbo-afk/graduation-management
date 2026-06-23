package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class StudentServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Auth.isAdmin(request)) {
            request.getRequestDispatcher("forbidden.jsp").forward(request, response);
            return;
        }

        request.setAttribute("students", dao.listStudents());
        request.getRequestDispatcher("students.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!Auth.isAdmin(request)) {
            request.setAttribute("message", "只有管理员可以新增学生");
            doGet(request, response);
            return;
        }

        String classId = request.getParameter("classId");
        String studentNo = request.getParameter("studentNo");
        String studentName = request.getParameter("studentName");
        String gender = request.getParameter("gender");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");

        boolean ok = dao.addStudent(classId, studentNo, studentName, gender, phone, email);
        if (ok) {
            response.sendRedirect("students");
        } else {
            request.setAttribute("message", "新增学生失败，请检查学号或账号是否重复");
            doGet(request, response);
        }
    }
}
