<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>查询统计</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>查询统计</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }

        User user = (User) session.getAttribute("user");
        if (user != null && ("admin".equals(user.getRoleName()) || "teacher".equals(user.getRoleName()))) {
    %>
    <form method="post" action="statistics" class="form-line">
        <label>学生ID：<input name="studentId" value="1"></label>
        <button type="submit">调用存储过程计算总成绩</button>
    </form>
    <%
        } else {
    %>
    <p>当前角色只能查看统计结果。</p>
    <%
        }
    %>
</div>
<div class="box">
    <h3>学生进度表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("progress"));
    %>
    <jsp:include page="table.jsp"/>
</div>
<div class="box">
    <h3>教师指导统计表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("teacherGuidance"));
    %>
    <jsp:include page="table.jsp"/>
</div>
<div class="box">
    <h3>成绩排名表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("scoreRank"));
    %>
    <jsp:include page="table.jsp"/>
</div>
<%
    if (user != null && ("admin".equals(user.getRoleName()) || "teacher".equals(user.getRoleName()))) {
%>
<div class="box">
    <h3>审计日志 operation_log</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("logs"));
    %>
    <jsp:include page="table.jsp"/>
</div>
<%
    }
%>
</body>
</html>
