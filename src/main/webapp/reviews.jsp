<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>教师评阅</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>教师评阅</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }

        User user = (User) session.getAttribute("user");
        if (user != null && "teacher".equals(user.getRoleName())) {
    %>
    <form method="post" action="reviews" class="form-line">
        <label>提交ID：<input name="submissionId" value="1"></label>
        <input type="hidden" name="teacherId" value="<%= user.getRelatedId() %>">
        <label>分数：<input name="score" value="85"></label>
        <label>评语：<textarea name="comment"></textarea></label>
        <button type="submit">提交评阅</button>
    </form>
    <%
        } else {
    %>
    <p>当前角色只能查看评阅记录。</p>
    <%
        }
    %>
</div>
<div class="box">
    <h3>待参考的提交列表</h3>
    <%
        List<Map<String, String>> submissions = (List<Map<String, String>>) request.getAttribute("submissions");
        if (submissions != null && submissions.size() > 0) {
    %>
    <table>
        <tr>
            <th>submission_id</th>
            <th>student_name</th>
            <th>stage_name</th>
            <th>title</th>
            <th>file</th>
            <th>submit_time</th>
            <th>status</th>
        </tr>
        <%
            for (Map<String, String> row : submissions) {
        %>
        <tr>
            <td><%= row.get("submission_id") %></td>
            <td><%= row.get("student_name") %></td>
            <td><%= row.get("stage_name") %></td>
            <td><%= row.get("title") %></td>
            <td>
                <%
                    String filePath = row.get("file_path");
                    if (filePath != null && filePath.length() > 0) {
                %>
                <a href="<%= filePath %>" target="_blank">打开文件</a>
                <%
                    } else {
                %>
                无
                <%
                    }
                %>
            </td>
            <td><%= row.get("submit_time") %></td>
            <td><%= row.get("status") %></td>
        </tr>
        <%
            }
        %>
    </table>
    <%
        } else {
    %>
    <p>暂无数据</p>
    <%
        }
    %>
</div>
<div class="box">
    <h3>评阅列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("reviews"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
