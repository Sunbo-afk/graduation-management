<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    List<Map<String, String>> tableData = (List<Map<String, String>>) request.getAttribute("tableData");
    if (tableData != null && tableData.size() > 0) {
%>
<table>
    <tr>
        <%
            Map<String, String> firstRow = tableData.get(0);
            for (String key : firstRow.keySet()) {
        %>
        <th><%= key %></th>
        <%
            }
        %>
    </tr>
    <%
        for (Map<String, String> row : tableData) {
    %>
    <tr>
        <%
            for (String key : row.keySet()) {
        %>
        <td><%= row.get(key) %></td>
        <%
            }
        %>
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
