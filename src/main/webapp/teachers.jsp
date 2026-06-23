<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>教师管理</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
<jsp:include page="nav.jsp"/>
<div class="box">
    <h2>教师管理</h2>
    <p>新增教师后，系统会自动创建同名账号，初始密码为 123456。</p>
    <%
        String message = (String) request.getAttribute("message");
        if (message != null) {
    %>
    <div class="message"><%= message %></div>
    <%
        }
    %>
    <form method="post" action="teachers" class="form-line">
        <label>系ID：<input name="departmentId" value="1"></label>
        <label>工号：<input name="teacherNo"></label>
        <label>姓名：<input name="teacherName"></label>
        <label>研究方向：<input name="direction"></label>
        <label>电话：<input name="phone"></label>
        <label>邮箱：<input name="email"></label>
        <button type="submit">新增教师</button>
    </form>
</div>
<div class="box">
    <h3>教师列表</h3>
    <%
        List<Map<String, String>> teachers = (List<Map<String, String>>) request.getAttribute("teachers");
        if (teachers != null && teachers.size() > 0) {
    %>
    <table>
        <tr>
            <th>teacher_id</th>
            <th>teacher_no</th>
            <th>teacher_name</th>
            <th>department_name</th>
            <th>research_direction</th>
            <th>操作</th>
        </tr>
        <%
            for (Map<String, String> row : teachers) {
        %>
        <tr>
            <td><%= row.get("teacher_id") %></td>
            <td><%= row.get("teacher_no") %></td>
            <td><%= row.get("teacher_name") %></td>
            <td><%= row.get("department_name") %></td>
            <td><%= row.get("research_direction") %></td>
            <td>
                <form method="post" action="teachers" onsubmit="return confirm('确定删除该教师吗？');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="teacherId" value="<%= row.get("teacher_id") %>">
                    <button type="submit">删除</button>
                </form>
            </td>
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
</body>
</html>
