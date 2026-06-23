<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="model.User" %>
<%
    User loginUser = (User) session.getAttribute("user");
    if (loginUser == null) {
        response.sendRedirect("login");
        return;
    }
%>
<div class="top">
    当前用户：<%= loginUser.getUsername() %>（<%= loginUser.getRoleName() %>）
    &nbsp;&nbsp;
    <a href="dashboard">首页</a>
    <%
        if ("admin".equals(loginUser.getRoleName())) {
    %>
    <a href="management">毕设管理</a>
    <a href="students">学生管理</a>
    <a href="teachers">教师管理</a>
    <%
        }

        if ("admin".equals(loginUser.getRoleName()) || "teacher".equals(loginUser.getRoleName()) || "student".equals(loginUser.getRoleName())) {
    %>
    <a href="topics">题目管理</a>
    <%
        }

        if ("student".equals(loginUser.getRoleName()) || "teacher".equals(loginUser.getRoleName()) || "admin".equals(loginUser.getRoleName())) {
    %>
    <a href="submissions">阶段提交</a>
    <%
        }

        if ("teacher".equals(loginUser.getRoleName()) || "admin".equals(loginUser.getRoleName())) {
    %>
    <a href="reviews">教师评阅</a>
    <%
        }

        if ("admin".equals(loginUser.getRoleName())) {
    %>
    <a href="notices">公告管理</a>
    <%
        }
    %>
    <a href="statistics">查询统计</a>
    <%
        if ("teacher".equals(loginUser.getRoleName()) || "student".equals(loginUser.getRoleName())) {
    %>
    <a href="password">修改密码</a>
    <%
        }
    %>
    <a href="logout">退出</a>
</div>
