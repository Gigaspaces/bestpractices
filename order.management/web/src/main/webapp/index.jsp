<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1" %>
<% String context = request.getContextPath();
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>GS REST Example</title>
</head>
<body>
<div align="center">
    <a href="<%=context%>/personsearch/">
        <img alt="logo" src="<%=context%>/resources/logo.png">
    </a>
</div>
<hr>
<a href="${context}/web/orderManagement/orders/*">Orders</a>
<a href="${context}/web/orderManagement/accounts/*">Accounts</a>

</body>
</html>