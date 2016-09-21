<!doctype html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Orders</title>
</head>
<body>
<table border="1">
    <c:forEach var="accountDatum" items="${accountData}">
        <tr>
            <td>${accountDatum.username}</td>
            <td>${accountDatum.balance}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>