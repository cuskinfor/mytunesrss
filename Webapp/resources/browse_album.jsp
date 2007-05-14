<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

<div class="body">

<h1 class="browse">
    <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
</h1>

<jsp:include page="/incl_error.jsp" />

<ul class="links">
    <li>
        <a href="${servletUrl}/browseArtist/<mt:encrypt key="${encryptionKey}">page=${cwfn:choose(empty param.artist, param.page, '1')}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
    </li>
    <c:if test="${registered}">
        <li>
            <a href="${servletUrl}/browseGenre/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseGenres"/></a>
        </li>
    </c:if>
    <c:if test="${empty sessionScope.playlist}">
        <li>
            <a href="${servletUrl}/startNewPlaylist/backUrl=${cwfn:encodeUrl(backUrl)}"><fmt:message key="newPlaylist"/></a>
        </li>
    </c:if>
    <c:if test="${!empty param.backUrl}">
        <li style="float:right;">
            <a href="${param.backUrl}"><fmt:message key="back"/></a>
        </li>
    </c:if>
</ul>

<jsp:include page="incl_playlist.jsp" />

<c:set var="pager" scope="request" value="${albumPager}" />
<c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum/page={index}" />
<c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.artist || !empty param.genre, '*', param.page)}" />
<jsp:include page="incl_pager.jsp" />

<form id="browse" action="" method="post">

	<fieldset>
    <input type="hidden" name="backUrl" value="${backUrl}" />
	</fieldset>

    <table class="select" cellspacing="0">
        <tr>
            <c:if test="${!empty sessionScope.playlist}">
                <th class="check"><input type="checkbox" name="none" value="none" onclick="selectAllByLoop('album', 1, ${fn:length(albums)}, this)" /></th>
            </c:if>
            <th class="active">
                <fmt:message key="albums"/>
                <c:if test="${!empty param.artist}"> <fmt:message key="with"/> "${mtfn:decode64(param.artist)}"</c:if>
                <c:if test="${!empty param.genre}"> <fmt:message key="in"/> "${mtfn:decode64(param.genre)}"</c:if>
            </th>
            <th><fmt:message key="artist"/></th>
            <th colspan="${1 + mtfn:buttonColumns(authUser, config)}"><fmt:message key="tracks"/></th>
        </tr>
        <c:forEach items="${albums}" var="album" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">
                        <input type="checkbox" name="album" id="album${loopStatus.count}" value="${mtfn:encode64(album.name)}" />
                    </td>
                </c:if>
                <td class="artist">
                    <c:out value="${cwfn:choose(mtfn:unknown(album.name), '(unknown)', album.name)}" />
                </td>
                <td>
                    <c:choose>
                        <c:when test="${album.artistCount == 1}">
                            <c:choose>
                                <c:when test="${singleArtist}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(album.artist), '(unknown)', album.artist)}" />
                                </c:when>
                                <c:otherwise>
                                    <a href="${servletUrl}/browseAlbum/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(album.artist))}</mt:encrypt>/backUrl=${cwfn:encodeUrl(backUrl)}">
                                        <c:out value="${cwfn:choose(mtfn:unknown(album.artist), '(unknown)', album.artist)}" /></a>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise><fmt:message key="variousArtists"/></c:otherwise>
                    </c:choose>
                </td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${cwfn:encodeUrl(backUrl)}"> ${album.trackCount} </a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:if test="${authUser.rss && config.showRss}">
                            <td class="icon">
                                <a href="${servletUrl}/createRSS/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="rss" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.playlist && config.showPlaylist}">
                            <td class="icon">
                                <a href="${servletUrl}/createPlaylist/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.${config.playlistFileSuffix}">
                                    <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="playlist" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.player && config.showPlayer}">
                            <td class="icon">
                                <a href="#" onclick="openPlayer('${appUrl}/flashplayer/xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url=${servletUrl}/createPlaylist/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(mtfn:encode64(album.name))}/type=Xspf</mt:encrypt>/${mtfn:virtualAlbumName(album)}.xspf')">
                                    <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="player" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.download && config.showDownload}">
                            <td class="icon">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || album.trackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>')">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${cwfn:encodeUrl(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:forEach>
        <c:if test="${(singleArtist || singleGenre) && fn:length(albums) > 1}">
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 0, 'even', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">&nbsp;</td>
                </c:if>
                <td colspan="2"><em><fmt:message key="allTracksOfAboveAlbums"/></em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${cwfn:encodeUrl(backUrl)}">${allAlbumsTrackCount}</a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:if test="${authUser.rss && config.showRss}">
                            <td class="icon">
                                <a href="${servletUrl}/createRSS/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="rss" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.playlist && config.showPlaylist}">
                            <td class="icon">
                                <a href="${servletUrl}/createPlaylist/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.${config.playlistFileSuffix}">
                                    <img src="${appUrl}/images/playlist${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="playlist" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.player && config.showPlayer}">
                            <td class="icon">
                                <a href="#" onclick="openPlayer('${appUrl}/flashplayer/xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url=${servletUrl}/createPlaylist/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/type=Xspf</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.xspf')">
                                    <img src="${appUrl}/images/player${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="player" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.download && config.showDownload}">
                            <td class="icon">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || allAlbumsTrackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/<mt:encrypt key="${encryptionKey}">auth=${cwfn:encodeUrl(auth)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>')">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${cwfn:encodeUrl(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request">${servletUrl}/browseAlbum/<mt:encrypt key="${encryptionKey}">page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index={index}/backUrl=${cwfn:encodeUrl(param.backUrl)}</c:set>
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

    <c:if test="${!empty sessionScope.playlist}">
        <div class="buttons">
            <input type="submit" onclick="document.forms['browse'].action = '${servletUrl}/addToPlaylist'" value="<fmt:message key="addSelected"/>" />
        </div>
    </c:if>

</form>

</div>

</body>

</html>
