package servlet;

import dao.SystemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.User;

import java.io.File;
import java.io.IOException;

@MultipartConfig
public class SubmissionServlet extends HttpServlet {
    private final SystemDao dao = new SystemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("submissions", dao.listSubmissions());
        request.setAttribute("stages", dao.listStages());
        request.getRequestDispatcher("submissions.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!Auth.isStudent(request)) {
            request.setAttribute("message", "只有学生可以提交阶段材料");
            doGet(request, response);
            return;
        }

        User user = Auth.getUser(request);
        String studentId = request.getParameter("studentId");
        if (user.getRelatedId() != null) {
            studentId = String.valueOf(user.getRelatedId());
        }

        if (!dao.hasSelectedTopic(studentId)) {
            request.setAttribute("message", "请先完成题目选择，再提交阶段材料");
            doGet(request, response);
            return;
        }

        String stageId = request.getParameter("stageId");
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String filePath = saveFile(request, studentId, stageId);

        if (filePath == null) {
            request.setAttribute("message", "只能上传 pdf、doc、docx 文件");
            doGet(request, response);
            return;
        }

        boolean ok = dao.addSubmission(studentId, stageId, title, content, filePath);
        if (ok) {
            response.sendRedirect("submissions");
        } else {
            request.setAttribute("message", "提交失败，同一学生同一阶段只能提交一次");
            doGet(request, response);
        }
    }

    private String saveFile(HttpServletRequest request, String studentId, String stageId) throws IOException, ServletException {
        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            return "";
        }

        String fileName = getFileName(filePart);
        String lowerName = fileName.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".doc") && !lowerName.endsWith(".docx")) {
            return null;
        }

        String ext = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0) {
            ext = fileName.substring(dotIndex);
        }

        String saveName = "student" + studentId + "_stage" + stageId + "_" + System.currentTimeMillis() + ext;
        String uploadPath = getServletContext().getRealPath("/uploads");
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File file = new File(uploadDir, saveName);
        filePart.write(file.getAbsolutePath());
        return "uploads/" + saveName;
    }

    private String getFileName(Part part) {
        String header = part.getHeader("content-disposition");
        String[] items = header.split(";");
        for (String item : items) {
            String text = item.trim();
            if (text.startsWith("filename")) {
                String name = text.substring(text.indexOf("=") + 1).trim();
                name = name.replace("\"", "");
                return name;
            }
        }
        return "";
    }
}
