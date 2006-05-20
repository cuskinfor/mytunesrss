<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

	<div class="pager">

		<c:if test="${!pager.first}">
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.firstPage.key)}"><img src="${appUrl}/images/pager_first.gif" alt="first"/></a>
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.previousPage.key)}"><img src="${appUrl}/images/pager_previous.gif" alt="previous"/></a>
		</c:if>
		<c:forEach items="${pager.currentPages}" var="page">
				<a href="${cwfn:choose(pagerCurrent == page.key, '#', mtfn:replace(pagerCommand, '{index}', page.key))}" <c:if test="${pagerCurrent == page.key}">class="active"</c:if>>
						<c:out value="${page.value}" /></a>
		</c:forEach>
		<c:if test="${!pager.last}">
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.nextPage.key)}"><img src="${appUrl}/images/pager_next.gif" alt="next"/></a>
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.lastPage.key)}"><img src="${appUrl}/images/pager_last.gif" alt="last"/></a>
		</c:if>

	</div>