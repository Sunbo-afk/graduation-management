<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>毕业设计管理系统登录</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<div class="box">
    <h2>毕业设计管理系统</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }
    %>
    <form method="post" action="login">
        <div>账号：<input type="text" name="username" value="admin"></div>
        <div>密码：<input type="password" name="password" value="123456"></div>
        <button type="submit">登录</button>
    </form>
    <p>测试账号：admin / 123456，teacher1 / 123456，student1 / 123456</p>
</div>
</body>
</html>
