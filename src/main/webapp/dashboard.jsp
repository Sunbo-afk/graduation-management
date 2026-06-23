<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>系统首页</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>系统首页</h2>
    <p>本系统用于毕业设计选题、阶段提交、教师评阅、公告发布和统计查询。</p>
</div>
<div class="box">
    <h3>公告</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("notices"));
    %>
    <jsp:include page="table.jsp"/>
</div>
<div class="box">
    <h3>教师指导统计视图</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("teacherGuidance"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
