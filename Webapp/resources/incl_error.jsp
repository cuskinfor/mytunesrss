<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:if test="${!empty errors}">
    <div class="error">
        <c:forEach items="${errors}" var="error">
            <c:choose>
                <c:when test="${error.localized}">
                    <c:set var="localizedMessage" value="${error.message}" />
                </c:when>
                <c:otherwise>
                    <fmt:message var="localizedMessage" key="${error.key}" />
                </c:otherwise>
            </c:choose>
            <c:out value="${cwfn:message(localizedMessage, error.parameters)}" />
        </c:forEach>
    </div>
    <c:remove var="errors" scope="session" />
</c:if>
