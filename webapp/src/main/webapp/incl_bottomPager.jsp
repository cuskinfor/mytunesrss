<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

	<div class="pager">

		<c:if test="${!pager.first}">
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.firstPage.key)}" class="first">First</a>
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.previousPage.key)}" class="previous">Previous</a>
		</c:if>
		<c:forEach items="${pager.currentPages}" var="page">
                <a href="${cwfn:choose(pagerCurrent == page.key, '#', mtfn:replace(pagerCommand, '{index}', page.key))}" <c:if test="${pagerCurrent == page.key}">class="active"</c:if>>
						<c:out value="${page.value}" /></a>
		</c:forEach>
		<c:if test="${!pager.last}">
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.nextPage.key)}" class="next">Next</a>
				<a href="${mtfn:replace(pagerCommand, '{index}', pager.lastPage.key)}" class="last">Last</a>
		</c:if>

	</div>