<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>公告管理</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>公告管理</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }
    %>
    <%
        User user = (User) session.getAttribute("user");
        if (user != null && "admin".equals(user.getRoleName())) {
    %>
        <form method="post" action="notices" class="form-line">
            <label>标题：<input name="title"></label>
            <label>内容：<textarea name="content"></textarea></label>
            <button type="submit">发布公告</button>
        </form>
    <%
        } else {
    %>
        <p>当前角色只能查看公告。</p>
    <%
        }
    %>
</div>
<div class="box">
    <h3>公告列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("notices"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
