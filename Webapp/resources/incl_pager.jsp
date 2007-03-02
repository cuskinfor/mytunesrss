<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<table class="pager" cellspacing="0">
    <tr>
        <c:forEach items="${pager.currentPages}" var="page">
            <td>
                <c:choose>
                    <%-- <c:when test="${page.userData > 0}"> --%>
                    <c:when test="${true}">
                        <a href="${cwfn:choose(pagerCurrent == page.key, '#', mtfn:replace(pagerCommand, '{index}', page.key))}"
                                <c:if test="${pagerCurrent == page.key}">class="active"</c:if>>
                            <c:out value="${page.value}" />
                        </a>
                    </c:when>
                    <c:otherwise>
                        <span><c:out value="${page.value}" /></span>
                    </c:otherwise>
                </c:choose>
            </td>
        </c:forEach>
        <td>
            <a href="${cwfn:choose(empty pagerCurrent, '#', mtfn:replace(pagerCommand, '{index}', ''))}"
                    <c:if test="${empty pagerCurrent}">class="active"</c:if>>
                <fmt:message key="alphabetPagerAll"/>
            </a>
        </td>
    </tr>
</table>
