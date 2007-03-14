<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/showPlaylistManager?index=${param.index}</c:set>
<c:set var="browseArtistUrl" scope="request">${servletUrl}/browseArtist?page=1</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

<div class="body">

    <h1 class="manager">
        <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <ul class="links">
        <li><a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:encodeUrl(browseArtistUrl)}"><fmt:message key="newPlaylist"/></a></li>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active"><fmt:message key="playlists"/></th>
						<c:if test="${!empty playlists}">
							<th colspan="4"><fmt:message key="tracks"/></th>
						</c:if>
        </tr>
        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td class="mytunes"><c:out value="${playlist.name}" /></td>
                <td class="tracks"><a href="${servletUrl}/browseTrack?playlist=${cwfn:encodeUrl(playlist.id)}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">${playlist.trackCount}</a></td>
                <td class="icon">
                    <a href="${servletUrl}/loadAndEditPlaylist?allowEditEmpty=true&amp;playlist=${cwfn:encodeUrl(playlist.id)}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">
                        <img src="${appUrl}/images/edit${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                </td>
                <td class="icon">
                    <a href="${servletUrl}/deletePlaylist/playlist=${cwfn:encodeUrl(playlist.id)}">
                        <img src="${appUrl}/images/delete${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="delete" /> </a>
                </td>
            </tr>
        </c:forEach>
			<c:if test="${empty playlists}">
				<tr><td><em><fmt:message key="noPlaylists"/></em></td></tr>
			</c:if>
    </table>

    <c:if test="${!empty pager}">
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPlaylistManager?index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

</body>

</html>
