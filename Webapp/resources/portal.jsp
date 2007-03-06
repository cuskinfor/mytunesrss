<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/showPortal&amp;index=${param.index}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title>
        <fmt:message key="applicationTitle" />
        v${mytunesrssVersion}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css?ts=${sessionCreationTime}" />
    <!--[if IE]>
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css?ts=${sessionCreationTime}" />
  <![endif]-->
    <script src="${appUrl}/js/functions.js?ts=${sessionCreationTime}" type="text/javascript"></script>

</head>

<body>

<div class="body">

    <h1 class="search"><span><fmt:message key="myTunesRss" /></span></h1>

    <ul class="links">
        <li><a href="${servletUrl}/showSettings">
            <fmt:message key="doSettings" />
        </a></li>
        <li><a href="${servletUrl}/browseServers">
            <fmt:message key="browseServers" />
        </a></li>
        <li style="float:right"><a href="${servletUrl}/logout">
            <fmt:message key="doLogout" />
        </a></li>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <form id="search" action="${servletUrl}/browseTrack" method="post">

        <table class="portal" cellspacing="0">
            <tr>
                <td class="search">
                    <input class="text" type="text" name="searchTerm" value="<c:out value="${param.searchTerm}"/>" style="width:120px;" />
                    <input type="hidden" name="backUrl" value="${backUrl}" /> <input class="button"
                                                                                     type="submit"
                                                                                     value="<fmt:message key="doSearch"/>" />
                </td>
                <td class="links">
                    <a href="${servletUrl}/browseArtist?page=1" style="background-image:url('${appUrl}/images/library_small.gif');">
                        <fmt:message key="browseLibrary" />
                    </a>
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <a href="${servletUrl}/showPlaylistManager" style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                <fmt:message key="managePlaylists" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/editPlaylist?backUrl=${cwfn:encodeUrl(backUrl)}"
                               style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                <fmt:message key="finishPlaylist" />
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${uploadLink}">
                        <a href="${servletUrl}/showUpload">
                        <fmt:message key="showUpload" />
                    </c:if>
                    </a>
                </td>
            </tr>
        </table>

    </form>

    <jsp:include page="incl_playlist.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active">
                <fmt:message key="playlists" />
            </th>
            <c:if test="${!empty playlists}">
                <th colspan="${1 + mtfn:buttonColumns(authUser, config)}">
                    <fmt:message key="tracks" />
                </th>
            </c:if>
        </tr>
        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td class="${fn:toLowerCase(playlist.type)}">
                    <c:out value="${playlist.name}" />
                </td>
                <td class="tracks">
                    <c:choose>
                        <c:when test="${playlist.trackCount >= 0}">
                            <a href="${servletUrl}/browseTrack?playlist=${cwfn:encodeUrl(playlist.id)}&amp;backUrl=${cwfn:encodeUrl(backUrl)}"> ${playlist.trackCount} </a>
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
                <c:if test="${authUser.rss && config.showRss}">
                    <td class="icon">
                        <a href="${servletUrl}/createRSS/auth=${cwfn:encodeUrl(auth)}/playlist=${cwfn:encodeUrl(playlist.id)}/${mtfn:webSafeFileName(playlist.name)}.xml">
                            <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="rss" /> </a>
                    </td>
                </c:if>
                <c:if test="${authUser.playlist && config.showPlaylist}">
                    <td class="icon">
                        <a href="${servletUrl}/createPlaylist/auth=${cwfn:encodeUrl(auth)}/playlist=${cwfn:encodeUrl(playlist.id)}/${mtfn:webSafeFileName(playlist.name)}.${config.playlistFileSuffix}">
                            <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="playlist" /> </a>
                    </td>
                </c:if>
                <c:if test="${authUser.player && config.showPlayer}">
                    <td class="icon">
                        <a href="#" onclick="openPlayer('${appUrl}/flashplayer/xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url=${servletUrl}/createPlaylist/auth=${cwfn:encodeUrl(auth)}/playlist=${cwfn:encodeUrl(playlist.id)}/type=Xspf/${mtfn:webSafeFileName(playlist.name)}.xspf')">
                            <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="playlist" /> </a>
                    </td>
                </c:if>
                <c:if test="${authUser.download && config.showDownload}">
                    <td class="icon">
                        <c:choose>
                            <c:when test="${authUser.maximumZipEntries <= 0 || playlist.trackCount <= authUser.maximumZipEntries}">
                                <a href="${servletUrl}/getZipArchive/auth=${cwfn:encodeUrl(auth)}/playlist=${cwfn:encodeUrl(playlist.id)}/${mtfn:webSafeFileName(playlist.name)}.zip">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="download"/>" /></a>
                            </c:when>
                            <c:otherwise>
                                <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>')">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
        <c:if test="${empty playlists}">
            <tr>
                <td><em>
                    <fmt:message key="noPlaylists" />
                </em></td>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty pager}">
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPortal?index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

</body>

</html>
