<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/showPortal&amp;index=${param.index}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="title" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
  <![endif]-->

</head>

<body>

<div class="body">

    <h1 class="search"><span>MyTunesRSS</span></h1>

    <jsp:include page="/incl_error.jsp" />

    <ul class="links">
        <li><a href="${servletUrl}/showSettings">settings</a></li>
        <li style="float:right"><a href="${servletUrl}/logout">logout</a></li>
    </ul>

    <form id="search" action="${servletUrl}/browseTrack" method="post">

        <table class="portal" cellspacing="0">
            <tr>
                <td class="search">
                    Search
                    <input class="text" type="text" name="searchTerm" value="<c:out value="${param.searchTerm}"/>" style="width:120px;" />
                    <input type="hidden" name="backUrl" value="${backUrl}" />
                    <input class="button" type="submit" value="search" />
                </td>
                <td class="links">
                    <a href="${servletUrl}/browseArtist?page=1"
                       style="background-image:url('${appUrl}/images/library_small.gif');"> browse library </a>
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <a href="${servletUrl}/showPlaylistManager" style="background-image:url('${appUrl}/images/feeds_small.gif');"> manage
                                                                                                                                           playlists </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/editPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"
                               style="background-image:url('${appUrl}/images/feeds_small.gif');"> finish playlist </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </table>

    </form>

    <jsp:include page="incl_playlist.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active">Playlists</th>
            <th colspan="${1+ fn:length(config.feedTypes)}">Tracks</th>
        </tr>
        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td class="${fn:toLowerCase(playlist.type)}"><c:out value="${playlist.name}" /></td>
                <td class="playlistTracks">${playlist.trackCount}</td>
                <c:forEach items="${config.feedTypes}" var="feedType">
                    <td class="icon">
                        <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/playlist=${playlist.id}/${mtfn:cleanFileName(playlist.name)}.${config.feedFileSuffix[feedType]}">
                            <img src="${appUrl}/images/${feedType}${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="${feedType}" /> </a>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
    </table>

    <c:if test="${!empty pager}">
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPortal?index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

</body>

</html>
