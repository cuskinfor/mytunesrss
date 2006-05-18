<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<table class="pager" cellspacing="0">
    <tr>
        <c:forEach items="${pagerItems}" var="item">
        <td><a href="${servletUrl}/${pagerCommand}?page=${item.key}" <c:if test="${pagerCurrent == item.key}">class="active"</c:if>>
            <c:out value="${item.value}" /></a></td>
        </c:forEach>
        <td><a href="${servletUrl}/${pagerCommand}" <c:if test="${pagerCurrent == 'all'}">class="active"</c:if>>all</a></td>
    </tr>
</table>
