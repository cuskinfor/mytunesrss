<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ attribute name="test" required="true" type="java.lang.Boolean" %>

<c:if test="${empty portalLinkCount}">
    <c:set var="portalLinkCount" scope="request" value="0"/>
</c:if>
<c:if test="${test}">
    <c:if test="${portalLinkCount % 3 == 0}"><td class="links"></c:if>
    <jsp:doBody />
    <c:if test="${portalLinkCount % 3 == 2}"></td></c:if>
    <c:set var="portalLinkCount" scope="request" value="${portalLinkCount + 1}"/>
</c:if>
