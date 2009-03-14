<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${albums}" var="album">
            <c:choose>
                <c:when test="${mtfn:unknown(album.name)}">
                    <c:set var="albumname"><fmt:message key="unknown"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="albumname" value="${album.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${albumname}" />" />
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
        <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${cwfn:choose(empty param.artist, param.page, '1')}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
    </li>
    <li>
        <a href="${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseGenres"/></a>
    </li>
    <c:if test="${empty sessionScope.playlist && authUser.createPlaylists}">
        <li>
            <c:choose>
                <c:when test="${empty editablePlaylists || simpleNewPlaylist}">
                    <a href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="newPlaylist"/></a>
                </c:when>
                <c:otherwise>
                    <a href="#" onclick="showDialog('editPlaylist', [function() {document.location.href='${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}'}, editExistingPlaylist, null])"><fmt:message key="editExistingPlaylist"/></a>
                </c:otherwise>
            </c:choose>
        </li>
    </c:if>
    <c:if test="${!empty param.backUrl}">
        <li style="float:right;">
            <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
        </li>
    </c:if>
</ul>

<jsp:include page="incl_playlist.jsp" />

<c:set var="pager" scope="request" value="${albumPager}" />
<c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum/${auth}/page={index}" />
<c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.artist || !empty param.genre, '*', param.page)}" />
<jsp:include page="incl_pager.jsp" />

<form id="browse" action="" method="post">

	<fieldset>
    <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
	</fieldset>

    <table class="select" cellspacing="0">
        <tr>
            <td colspan="${5 + cwfn:choose(!empty sessionScope.playlist, 1, 0)}" style="padding:0">
                <c:set var="displayFilterUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index=${param.index}/backUrl=${param.backUrl}</c:set>
                <jsp:include page="/incl_display_filter.jsp"/>
            </td>
        </tr>
        <tr>
            <c:if test="${!empty sessionScope.playlist}">
                <th class="check"><input type="checkbox" name="none" value="none" onclick="selectAllByLoop('album', 1, ${fn:length(albums)}, this)" /></th>
            </c:if>
            <th colspan="2" class="active">
                <c:if test="${!empty param.genre}">${mtfn:capitalize(mtfn:decode64(param.genre))}</c:if>
                <fmt:message key="albums"/>
                <c:if test="${!empty param.artist}"> <fmt:message key="with"/> "${cwfn:choose(mtfn:unknown(mtfn:decode64(param.artist)), msgUnknown, mtfn:decode64(param.artist))}"</c:if>
            </th>
            <th><fmt:message key="artist"/></th>
            <th colspan="2"><fmt:message key="tracks"/></th>
        </tr>
        <c:forEach items="${albums}" var="album" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">
                        <input type="checkbox" name="album" id="album${loopStatus.count}" value="${mtfn:encode64(album.name)}" />
                    </td>
                </c:if>
                <td class="albumthumb">
                    <c:choose>
                        <c:when test="${album.image}">
                            <img id="albumthumb_${loopStatus.index}" src="${servletUrl}/showAlbumImage/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(album.name)}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                            <div class="tooltip" id="tooltip_albumthumb_${loopStatus.index}"><img src="${servletUrl}/showAlbumImage/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(album.name)}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="albumname">
                    <c:choose>
                        <c:when test="${mtfn:unknown(album.name)}">
                            <fmt:message key="unknown"/>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${album.name}"/></a>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${album.artistCount == 1}">
                            <c:choose>
                                <c:when test="${singleArtist}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(album.artist), msgUnknown, album.artist)}" />
                                </c:when>
                                <c:otherwise>
                                    <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(album.artist))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                        <c:out value="${cwfn:choose(mtfn:unknown(album.artist), msgUnknown, album.artist)}" /></a>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise><fmt:message key="variousArtists"/></c:otherwise>
                    </c:choose>
                </td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${album.trackCount} </a>
                </td>
                <td class="icon">
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <c:if test="${authUser.remoteControl && config.remoteControl}">
                                <a style="cursor:pointer" onclick="jsonRpc('${servletUrl}', 'VideoLanClientService.loadAlbum', ['${album.name}', true])">
                                    <img src="${appUrl}/images/remote_control${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.rss && config.showRss}">
                                <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.playlist && config.showPlaylist}">
                                <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.${config.playlistFileSuffix}">
                                    <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.player && config.showPlayer}">
                                <a href="#" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:virtualAlbumName(album)}.xspf</mt:encrypt>'); return false">
                                    <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || album.trackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/addToPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add"/> </a>
                            <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}/name=${cwfn:encodeUrl(album.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/one_click_playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="oneClickPlaylist" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${(singleArtist || singleGenre) && fn:length(albums) > 1}">
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 0, 'even', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">&nbsp;</td>
                </c:if>
                <td>
                    &nbsp;
                </td>
                <td colspan="2"><em>
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="allTracksOfAboveAlbums"/></a>
                </em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${allAlbumsTrackCount}</a>
                </td>
                <td class="icon">
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <c:if test="${authUser.remoteControl && config.remoteControl}">
                                <a style="cursor:pointer" onclick="jsonRpc('${servletUrl}', 'VideoLanClientService.loadArtist', ['${mtfn:decode64(param.artist)}', true, true])">
                                    <img src="${appUrl}/images/remote_control${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.rss && config.showRss}">
                                <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.playlist && config.showPlaylist}">
                                <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.${config.playlistFileSuffix}">
                                    <img src="${appUrl}/images/playlist${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.player && config.showPlayer}">
                                <a href="#" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.xspf</mt:encrypt>'); return false">
                                    <img src="${appUrl}/images/player${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || allAlbumsTrackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/Albums%20with%20${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/addToPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                            <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/name=${cwfn:encodeUrl(param.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/one_click_playlist${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="oneClickPlaylist" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 0, 'odd', 'even')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">&nbsp;</td>
                </c:if>
                <td>
                    &nbsp;
                </td>
                <td colspan="2"><em>
                    <c:choose>
                        <c:when test="${singleArtist}">
                            <mt:array var="params">
                                <mt:arrayElement value="${mtfn:decode64(param.artist)}"/>
                            </mt:array>
                            <fmt:message var="localizedMessage" key="allTracksOfArtist"/>
                        </c:when>
                        <c:otherwise>
                            <mt:array var="params">
                                <mt:arrayElement value="${mtfn:decode64(param.genre)}"/>
                            </mt:array>
                            <fmt:message var="localizedMessage" key="allTracksOfGenre"/>
                        </c:otherwise>
                    </c:choose>
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${cwfn:message(localizedMessage, params)}"/></a>
                </em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${allArtistGenreTrackCount}</a>
                </td>
                <td class="icon">
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <c:if test="${authUser.remoteControl && config.remoteControl}">
                                <a style="cursor:pointer" onclick="jsonRpc('${servletUrl}', 'VideoLanClientService.loadArtist', ['${mtfn:decode64(param.artist)}', false, true])">
                                    <img src="${appUrl}/images/remote_control${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.rss && config.showRss}">
                                <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif"
                                         alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.playlist && config.showPlaylist}">
                                <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.${config.playlistFileSuffix}">
                                    <img src="${appUrl}/images/playlist${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif"
                                         alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.player && config.showPlayer}">
                                <a href="#" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.xspf</mt:encrypt>'); return false">
                                    <img src="${appUrl}/images/player${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif"
                                         alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || allArtistGenreTrackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/${mtfn:webSafeFileName(mtfn:decode64(param.artist))}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '_odd', '')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/addToPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif" alt="add" /> </a>
                            <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/name=${cwfn:encodeUrl(param.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/one_click_playlist${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif" alt="oneClickPlaylist" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

    <c:if test="${!empty sessionScope.playlist}">
        <div class="buttons">
            <input type="submit" onclick="document.forms['browse'].action = '${servletUrl}/addToPlaylist/${auth}'" value="<fmt:message key="addSelected"/>" />
        </div>
    </c:if>

</form>

</div>

<div id="glasspane" class="glasspane">
</div>

<jsp:include page="incl_edit_playlist_dialog.jsp"/>

</body>

</html>
