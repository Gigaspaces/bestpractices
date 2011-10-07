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
    <c:forEach var="orderEvent" items="${orderEvents}">
        <tr>
            <td>${orderEvent.id}</td>
            <td>${orderEvent.userName}</td>
            <td>${orderEvent.price}</td>
            <td>${orderEvent.operation}</td>
            <td>${orderEvent.status}</td>
        </tr>
    </c:forEach>
</table>
</body>
</html>