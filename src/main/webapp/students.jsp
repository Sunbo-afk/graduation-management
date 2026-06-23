<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>学生管理</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>学生管理</h2>
    <p>新增学生后，系统会自动创建同名账号，初始密码为 123456。</p>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }

        User user = (User) session.getAttribute("user");
        if (user != null && "admin".equals(user.getRoleName())) {
    %>
    <form method="post" action="students" class="form-line">
        <label>班级ID：<input name="classId" value="1"></label>
        <label>学号：<input name="studentNo"></label>
        <label>姓名：<input name="studentName"></label>
        <label>性别：<input name="gender"></label>
        <label>电话：<input name="phone"></label>
        <label>邮箱：<input name="email"></label>
        <button type="submit">新增学生</button>
    </form>
    <%
        }
    %>
</div>
<div class="box">
    <h3>学生列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("students"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
