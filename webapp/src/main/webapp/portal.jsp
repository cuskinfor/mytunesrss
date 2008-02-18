<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="mtt" %>

<c:set var="backUrl" scope="request">${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

<div class="body">

    <h1 class="search" onclick="window.open('http://www.codewave.de')" style="cursor: pointer"><span><fmt:message key="myTunesRss" /></span></h1>

    <ul class="links">
        <li><a href="${servletUrl}/showSettings/${auth}">
            <fmt:message key="doSettings" />
        </a></li>
        <c:if test="${registered}">
            <li><a href="${servletUrl}/browseServers/${auth}">
                <fmt:message key="browseServers" />
            </a></li>
        </c:if>
        <li style="float:right"><a href="${servletUrl}/logout">
            <fmt:message key="doLogout" />
        </a></li>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <form id="search" action="${servletUrl}/browseTrack/${auth}" method="post">

        <table class="portal" cellspacing="0">
            <tr>
                <td class="search">
                    <input class="text" type="text" name="searchTerm" value="<c:out value="${param.searchTerm}"/>" />
                    <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" /> <input class="button"
                                                                                     type="submit"
                                                                                     value="<fmt:message key="doSearch"/>" />
                </td>
                <td class="links">
                    <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=1</mt:encrypt>" style="background-image:url('${appUrl}/images/library_small.gif');">
                        <fmt:message key="browseLibrary" />
                    </a>
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <a href="${servletUrl}/showPlaylistManager/${auth}" style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                <fmt:message key="managePlaylists" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/editPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"
                               style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                <fmt:message key="finishPlaylist" />
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${uploadLink}">
                        <a href="${servletUrl}/showUpload/${auth}" style="background-image:url('${appUrl}/images/upload_small.gif');">
                            <fmt:message key="showUpload" />
                        </a>
                    </c:if>
                </td>
            </tr>
        </table>

    </form>

    <jsp:include page="incl_playlist.jsp" />

    <table cellspacing="0">
        <c:if test="${!empty statistics}">
            <tr>
                <th class="active" colspan="${2 + mtfn:buttonColumns(authUser, config)}" align="right">
                    <fmt:message key="statistics" />
                </th>
            </tr>
            <tr id="statistics">
                <td colspan="${2 + mtfn:buttonColumns(authUser, config)}">
                    <table class="statistics">
                        <tr>
                            <td><fmt:message key="statistics.tracks" />: ${statistics.trackCount}</td>
                            <td><fmt:message key="statistics.albums" />: ${statistics.albumCount}</td>
                            <td><fmt:message key="statistics.artists" />: ${statistics.artistCount}</td>
                            <td style="width:100%"><fmt:message key="statistics.genres" />: ${statistics.genreCount}</td>
                        </tr>
                    </table>
                </td>
            </tr>
        </c:if>
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
                            <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${playlist.trackCount} </a>
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
                <c:if test="${authUser.rss && config.showRss}">
                    <td class="icon">
                        <a href="${permServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.xml">
                            <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                    </td>
                </c:if>
                <c:if test="${authUser.playlist && config.showPlaylist}">
                    <td class="icon">
                        <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.${config.playlistFileSuffix}">
                            <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                    </td>
                </c:if>
                <c:if test="${authUser.player && config.showPlayer}">
                    <td class="icon">
                        <a href="#" onclick="openPlayer('<mtt:flashplayer playlistParams="playlist=${playlist.id}" filename="${mtfn:webSafeFileName(playlist.name)}.xspf"/>'); return false">
                            <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                    </td>
                </c:if>
                <c:if test="${authUser.download && config.showDownload}">
                    <td class="icon">
                        <c:choose>
                            <c:when test="${authUser.maximumZipEntries <= 0 || playlist.trackCount <= authUser.maximumZipEntries}">
                                <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.zip">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                            </c:when>
                            <c:otherwise>
                                <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
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
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPortal/${auth}/index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

</body>

</html>
