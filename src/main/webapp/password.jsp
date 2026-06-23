<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>修改密码</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>修改密码</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }
    %>
    <form method="post" action="password" class="form-line">
        <label>原密码：<input type="password" name="oldPassword"></label>
        <label>新密码：<input type="password" name="newPassword"></label>
        <label>确认新密码：<input type="password" name="confirmPassword"></label>
        <button type="submit">保存</button>
    </form>
</div>
</body>
</html>
