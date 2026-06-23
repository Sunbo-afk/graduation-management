<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>阶段提交</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>阶段提交</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }

        User user = (User) session.getAttribute("user");
        if (user != null && "student".equals(user.getRoleName())) {
    %>
    <form method="post" action="submissions" class="form-line" enctype="multipart/form-data">
        <input type="hidden" name="studentId" value="<%= user.getRelatedId() %>">
        <label>阶段ID：<input name="stageId" value="1"></label>
        <label>标题：<input name="title"></label>
        <div class="content-field">
            内容：
            <div class="content-box">
                <textarea name="content"></textarea>
                <input type="file" name="file" accept=".pdf,.doc,.docx">
            </div>
        </div>
        <button type="submit">提交材料</button>
    </form>
    <%
        } else {
    %>
    <p>当前角色只能查看阶段提交记录。</p>
    <%
        }
    %>
</div>
<div class="box">
    <h3>阶段提交列表</h3>
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
    <h3>阶段字典</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("stages"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
