<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum?artist=${cwfn:encodeUrl(param.artist)}&amp;genre=${cwfn:encodeUrl(param.genre)}&amp;page=${param.page}&amp;index=${param.index}</c:set>

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

<ul class="links">
    <li>
        <a href="${servletUrl}/browseArtist?page=${cwfn:choose(empty param.artist, param.page, '1')}"><fmt:message key="browseArtist"/></a>
    </li>
    <li>
        <a href="${servletUrl}/browseGenre?page=${param.page}"><fmt:message key="browseGenres"/></a>
    </li>
    <c:if test="${empty sessionScope.playlist}">
        <li>
            <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:encodeUrl(backUrl)}"><fmt:message key="newPlaylist"/></a>
        </li>
    </c:if>
</ul>

<jsp:include page="incl_playlist.jsp" />

<c:set var="pager" scope="request" value="${albumPager}" />
<c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum?page={index}" />
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
                <c:if test="${!empty param.artist}"> <fmt:message key="with"/> "${cwfn:decode64(param.artist)}"</c:if>
                <c:if test="${!empty param.genre}"> <fmt:message key="in"/> "${cwfn:decode64(param.genre)}"</c:if>
            </th>
            <th><fmt:message key="artist"/></th>
            <th colspan="${cwfn:choose(config.showDownload, 2, 1) + config.feedTypeCount}"><fmt:message key="tracks"/></th>
        </tr>
        <c:forEach items="${albums}" var="album" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">
                        <input type="checkbox" name="album" id="album${loopStatus.count}" value="${cwfn:encode64(album.name)}" />
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
                                    <a href="${servletUrl}/browseAlbum?artist=${cwfn:encodeUrl(cwfn:encode64(album.artist))}">
                                        <c:out value="${cwfn:choose(mtfn:unknown(album.artist), '(unknown)', album.artist)}" /></a>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise><fmt:message key="variousArtists"/></c:otherwise>
                    </c:choose>

                </td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack?album=${cwfn:encodeUrl(cwfn:encode64(album.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}"> ${album.trackCount} </a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:if test="${authUser.rss && config.showRss}">
                            <td class="icon">
                                <a href="${servletUrl}/createRSS/auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(cwfn:encode64(album.name))}/${mtfn:virtualAlbumName(album)}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="rss" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.m3u && config.showM3u}">
                            <td class="icon">
                                <a href="${servletUrl}/createM3U/auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(cwfn:encode64(album.name))}/${mtfn:virtualAlbumName(album)}.m3u">
                                    <img src="${appUrl}/images/m3u${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="m3u" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.download && config.showDownload}">
                            <td class="icon">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || album.trackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/auth=${cwfn:encodeUrl(auth)}/album=${cwfn:encodeUrl(cwfn:encode64(album.name))}/${mtfn:virtualAlbumName(album)}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"/>')">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist?album=${cwfn:encodeUrl(cwfn:encode64(album.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:forEach>
        <c:if test="${singleArtist && fn:length(albums) > 1}">
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 0, 'even', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">&nbsp;</td>
                </c:if>
                <td colspan="2"><em><fmt:message key="allTracksOfAboveAlbums"/></em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack?fullAlbums=true&amp;artist=${cwfn:encodeUrl(param.artist)}&amp;genre=${cwfn:encodeUrl(param.genre)}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">${singleArtistTrackCount}</a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:if test="${authUser.rss && config.showRss}">
                            <td class="icon">
                                <a href="${servletUrl}/createRSS/auth=${cwfn:encodeUrl(auth)}/fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/${mtfn:webSafeFileName(cwfn:decode64(param.artist))}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="rss" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.m3u && config.showM3u}">
                            <td class="icon">
                                <a href="${servletUrl}/createM3U/auth=${cwfn:encodeUrl(auth)}/fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/${mtfn:webSafeFileName(cwfn:decode64(param.artist))}.m3u">
                                    <img src="${appUrl}/images/m3u${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="m3u" /> </a>
                            </td>
                        </c:if>
                        <c:if test="${authUser.download && config.showDownload}">
                            <td class="icon">
                                <c:choose>
                                    <c:when test="${authUser.maximumZipEntries <= 0 || singleArtistTrackCount.trackCount <= authUser.maximumZipEntries}">
                                        <a href="${servletUrl}/getZipArchive/auth=${cwfn:encodeUrl(auth)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/${mtfn:webSafeFileName(cwfn:decode64(param.artist))}.zip">
                                            <img src="${appUrl}/images/download${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="#" onclick="alert('<fmt:message key="error.zipLimit"/>')">
                                            <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist?fullAlbums=true&amp;artist=${cwfn:encodeUrl(param.artist)}&amp;genre=${cwfn:encodeUrl(param.genre)}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum?page=${param.page}&amp;artist=${cwfn:encodeUrl(param.artist)}&amp;genre=${cwfn:encodeUrl(param.genre)}&amp;index={index}" />
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
