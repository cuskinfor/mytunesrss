<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl" scope="request">${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${genres}" var="genre">
            <c:choose>
                <c:when test="${mtfn:unknown(genre.name)}">
                    <c:set var="genrename"><fmt:message key="unknown"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="genrename" value="${genre.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/${mtfn:virtualGenreName(genre)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${genrename}" />" />
        </c:forEach>
    </c:if>

</head>

<body>

<div class="body">

    <h1 class="browse">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <ul class="links">
        <li>
            <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
        </li>
        <li>
            <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseAlbums"/></a>
        </li>
        <c:if test="${empty sessionScope.playlist && authUser.createPlaylists}">
            <li>
                <c:choose>
                    <c:when test="${empty editablePlaylists || simpleNewPlaylist}">
                        <a href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="newPlaylist"/></a>
                    </c:when>
                    <c:otherwise>
                        <a style="cursor:pointer" onclick="showDialog('editPlaylist', [function() {document.location.href='${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}'}, editExistingPlaylist, null])"><fmt:message key="editExistingPlaylist"/></a>
                    </c:otherwise>
                </c:choose>
            </li>
        </c:if>
    </ul>

    <jsp:include page="incl_playlist.jsp" />

    <c:set var="pager" scope="request" value="${genrePager}" />
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseGenre/${auth}/page={index}" />
    <c:set var="pagerCurrent" scope="request" value="${param.page}" />
    <jsp:include page="incl_pager.jsp" />

    <form id="browse" action="" method="post">

			<fieldset>
            <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
			</fieldset>

        <table class="select" cellspacing="0">
            <tr>
                <th class="active">
                    <fmt:message key="genres"/>
                </th>
                <th><fmt:message key="albums"/></th>
                <th><fmt:message key="artists"/></th>
                <th colspan="2"><fmt:message key="tracks"/></th>
            </tr>
            <c:forEach items="${genres}" var="genre" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td class="genre">
                        <c:out value="${genre.name}" />
                    </td>
                    <td class="album">
                        <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.albumCount} </a>
                    </td>
                    <td class="genreartist">
                        <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.artistCount} </a>
                    </td>
                    <td class="tracks">
                        <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.trackCount} </a>
                    </td>
                    <td class="icon">
                        <c:choose>
                            <c:when test="${empty sessionScope.playlist}">
                                <c:if test="${authUser.remoteControl && config.remoteControl}">
                                    <a style="cursor:pointer" onclick="jsonRpc('${servletUrl}', 'VideoLanClientService.loadGenre', ['${genre.name}', true])">
                                        <img src="${appUrl}/images/remote_control${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                                </c:if>
                                <c:if test="${authUser.rss && config.showRss}">
                                    <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/${mtfn:virtualGenreName(genre)}.xml">
                                        <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                                </c:if>
                                <c:if test="${authUser.playlist && config.showPlaylist}">
                                    <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/${mtfn:virtualGenreName(genre)}.${config.playlistFileSuffix}">
                                        <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                                </c:if>
                                <c:if test="${authUser.player && config.showPlayer}">
                                    <a style="cursor:pointer" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:virtualGenreName(genre)}.xspf</mt:encrypt>'); return false">
                                        <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                                </c:if>
                                <c:if test="${authUser.download && config.showDownload}">
                                    <c:choose>
                                        <c:when test="${authUser.maximumZipEntries <= 0 || genre.trackCount <= authUser.maximumZipEntries}">
                                            <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/${mtfn:virtualGenreName(genre)}.zip">
                                                <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                        </c:when>
                                        <c:otherwise>
                                            <a style="cursor:pointer" onclick="alert('<fmt:message key="error.zipLimit"/>'); return false">
                                                <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <a href="${servletUrl}/addToPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                                <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}/name=${cwfn:encodeUrl(genre.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <img src="${appUrl}/images/one_click_playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </table>

        <c:if test="${!empty indexPager}">
            <c:set var="pager" scope="request" value="${indexPager}" />
            <c:set var="pagerCommand" scope="request">${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>/index={index}</c:set>
            <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
            <jsp:include page="incl_bottomPager.jsp" />
        </c:if>

    </form>

</div>

<div id="glasspane" class="glasspane">
</div>

<jsp:include page="incl_edit_playlist_dialog.jsp"/>

</body>

</html>
