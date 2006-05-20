<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<table class="pager" cellspacing="0">
    <tr>
			<c:forEach items="${pager.currentPages}" var="page">
				<td>
					<c:choose>
						<c:when test="${page.userData.active}">
							<a href="${cwfn:choose(pagerCurrent == page.key, '#', mtfn:replace(pagerCommand, '{index}', page.key))}" <c:if test="${pagerCurrent == page.key}">class="active"</c:if>>
								<c:out value="${page.value}" />
							</a>
						</c:when>
						<c:otherwise>
							<span><c:out value="${page.value}" /></span>
						</c:otherwise>
					</c:choose>
				</td>
			</c:forEach>
    </tr>
</table>
