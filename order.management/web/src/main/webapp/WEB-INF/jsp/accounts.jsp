<!doctype html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Orders</title>
</head>
<body>
<table border="1">
    <c:forEach var="accountDatum" items="${accountData}">
        <tr>
            <td>${accountDatum.userName}</td>
            <td>${accountDatum.balance}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>