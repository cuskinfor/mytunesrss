<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<table class="pager" cellspacing="0">
    <tr>
        <c:if test="${!pager.first}">
            <td><a href="${mtfn:replace(pagerCommand, '{index}', pager.firstPage.key)}">&lt;&lt;</a></td>
            <td><a href="${mtfn:replace(pagerCommand, '{index}', pager.previousPage.key)}">&lt;</a></td>
        </c:if>
        <c:forEach items="${pager.currentPages}" var="page">
            <td><a href="${cwfn:choose(pagerCurrent == page.key, '#', mtfn:replace(pagerCommand, '{index}', page.key))}" <c:if test="${pagerCurrent == page.key}">class="active"</c:if>>
                <c:out value="${page.value}" /></a></td>
        </c:forEach>
        <c:if test="${!pager.last}">
            <td><a href="${mtfn:replace(pagerCommand, '{index}', pager.nextPage.key)}">&gt;</a></td>
            <td><a href="${mtfn:replace(pagerCommand, '{index}', pager.lastPage.key)}">&gt;&gt;</a></td>
        </c:if>
    </tr>
</table>
