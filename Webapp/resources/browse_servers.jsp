<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum?artist=${cwfn:encodeUrl(param.artist)}&amp;page=${param.page}&amp;index=${param.index}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="applicationTitle" /> v${mytunesrssVersion}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css?ts=${sessionCreationTime}" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css?ts=${sessionCreationTime}" />
    <![endif]-->
    <script src="${appUrl}/js/functions.js?ts=${sessionCreationTime}" type="text/javascript"></script>

</head>

<body>

<div class="body">

<h1 class="browse">
    <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
</h1>

<jsp:include page="/incl_error.jsp" />

<jsp:include page="incl_playlist.jsp" />

<form id="browse" action="" method="post">

	<fieldset>
    <input type="hidden" name="backUrl" value="${backUrl}" />
	</fieldset>

    <table class="select" cellspacing="0">
        <tr>
            <th class="active">
                <fmt:message key="serverName"/>
            </th>
            <th><fmt:message key="serverAddress"/></th>
        </tr>
        <c:forEach items="${servers}" var="server" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td>
                    <a href="http://${server.address}:${server.port}"><c:out value="${server.name}" /></a>
                </td>
                <td>
                    <a href="http://${server.address}:${server.port}">${server.address}</a>
                </td>
            </tr>
        </c:forEach>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseServers?page=${param.page}&amp;index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</form>

</div>

</body>

</html>
