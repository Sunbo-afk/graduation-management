<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>毕设管理</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>毕设管理</h2>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }
    %>
</div>

<div class="box">
    <h3>为指导教师分配毕业生</h3>
    <p>通过“学生ID + 课题ID”完成分配。课题属于某个教师，因此学生选中课题后，也就分配给了该指导教师。</p>
    <form method="post" action="management" class="form-line">
        <input type="hidden" name="action" value="assign">
        <label>学生ID：<input name="studentId" value="1"></label>
        <label>课题ID：<input name="topicId" value="1"></label>
        <button type="submit">分配</button>
    </form>
</div>

<div class="box">
    <h3>设置阶段提交期限</h3>
    <form method="post" action="management" class="form-line">
        <input type="hidden" name="action" value="deadline">
        <label>系ID：<input name="departmentId" value="1"></label>
        <label>阶段ID：<input name="stageId" value="1"></label>
        <label>开始时间：<input name="startTime" value="2026-03-01 00:00:00"></label>
        <label>截止时间：<input name="endTime" value="2026-03-20 23:59:59"></label>
        <button type="submit">保存期限</button>
    </form>
</div>

<div class="box">
    <h3>公布毕业设计公告、要求</h3>
    <form method="post" action="management" class="form-line">
        <input type="hidden" name="action" value="notice">
        <label>标题：<input name="title"></label>
        <div class="content-field">
            内容：
            <div class="content-box">
                <textarea name="content"></textarea>
            </div>
        </div>
        <button type="submit">发布</button>
    </form>
</div>

<div class="box">
    <h3>学生列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("students"));
    %>
    <jsp:include page="table.jsp"/>
</div>

<div class="box">
    <h3>教师列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("teachers"));
    %>
    <jsp:include page="table.jsp"/>
</div>

<div class="box">
    <h3>课题列表</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("topics"));
    %>
    <jsp:include page="table.jsp"/>
</div>

<div class="box">
    <h3>阶段字典</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("stages"));
    %>
    <jsp:include page="table.jsp"/>
</div>

<div class="box">
    <h3>阶段期限</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("deadlines"));
    %>
    <jsp:include page="table.jsp"/>
</div>

<div class="box">
    <h3>公告要求</h3>
    <%
        request.setAttribute("tableData", request.getAttribute("notices"));
    %>
    <jsp:include page="table.jsp"/>
</div>
</body>
</html>
