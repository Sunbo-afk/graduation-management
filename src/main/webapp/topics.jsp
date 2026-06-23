<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>题目管理</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>题目管理</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }

        User user = (User) session.getAttribute("user");
        if (user != null && ("teacher".equals(user.getRoleName()) || "admin".equals(user.getRoleName()))) {
    %>
    <form method="post" action="topics" class="form-line">
        <%
            if ("admin".equals(user.getRoleName())) {
        %>
        <label>教师ID：<input name="teacherId" value="1"></label>
        <%
            } else {
        %>
        <input type="hidden" name="teacherId" value="<%= user.getRelatedId() %>">
        <%
            }
        %>
        <label>题目：<input name="title"></label>
        <label>简介：<input name="desc"></label>
        <label>技能：<input name="skill"></label>
        <label>人数上限：<input name="maxStudents" value="1"></label>
        <button type="submit">新增题目</button>
    </form>
    <%
        } else {
    %>
    <p>当前角色不能新增题目。</p>
    <%
        }
    %>
</div>
<%
    if (user != null && "student".equals(user.getRoleName())) {
%>
<div class="box">
    <h3>学生选题</h3>
    <form method="post" action="topics" class="form-line">
        <input type="hidden" name="action" value="select">
        <input type="hidden" name="studentId" value="<%= user.getRelatedId() %>">
        <label>题目ID：<input name="topicId" value="1"></label>
        <button type="submit">执行选题</button>
    </form>
</div>
<%
    }
%>
<div class="box">
    <h3>题目列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("topics"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
