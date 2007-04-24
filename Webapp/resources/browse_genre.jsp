<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseGenre?page=${param.page}&amp;index=${param.index}</c:set>

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
            <a href="${servletUrl}/browseArtist?page=${param.page}"><fmt:message key="browseArtist"/></a>
        </li>
        <li>
            <a href="${servletUrl}/browseAlbum?page=${param.page}"><fmt:message key="browseAlbums"/></a>
        </li>
        <c:if test="${empty sessionScope.playlist}">
            <li>
                <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:encodeUrl(backUrl)}"><fmt:message key="newPlaylist"/></a>
            </li>
        </c:if>
    </ul>

    <jsp:include page="incl_playlist.jsp" />

    <c:set var="pager" scope="request" value="${genrePager}" />
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseGenre?page={index}" />
    <c:set var="pagerCurrent" scope="request" value="${param.page}" />
    <jsp:include page="incl_pager.jsp" />

    <form id="browse" action="" method="post">

			<fieldset>
        <input type="hidden" name="backUrl" value="${backUrl}" />
			</fieldset>

        <table class="select" cellspacing="0">
            <tr>
                <th class="active">
                    <fmt:message key="genres"/>
                </th>
                <th><fmt:message key="albums"/></th>
                <th><fmt:message key="artists"/></th>
                <th colspan="${1 + mtfn:buttonColumns(authUser, config)}"><fmt:message key="tracks"/></th>
            </tr>
            <c:forEach items="${genres}" var="genre" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td class="genre">
                        <c:out value="${genre.name}" />
                    </td>
                    <td class="album">
                        <a href="${servletUrl}/browseAlbum?genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}"> ${genre.albumCount} </a>
                    </td>
                    <td class="genreartist">
                        <a href="${servletUrl}/browseArtist?genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}"> ${genre.artistCount} </a>
                    </td>
                    <td class="tracks">
                        <a href="${servletUrl}/browseTrack?genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}"> ${genre.trackCount} </a>
                    </td>
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <c:if test="${authUser.rss && config.showRss}">
                                <td class="icon">
                                    <a href="${servletUrl}/createRSS/auth=${cwfn:encodeUrl(auth)}/genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}/${mtfn:virtualGenreName(genre)}.xml">
                                        <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="rss" /> </a>
                                </td>
                            </c:if>
                            <c:if test="${authUser.playlist && config.showPlaylist}">
                                <td class="icon">
                                    <a href="${servletUrl}/createPlaylist/auth=${cwfn:encodeUrl(auth)}/genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}/${mtfn:virtualGenreName(genre)}.${config.playlistFileSuffix}">
                                        <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="playlist" /> </a>
                                </td>
                            </c:if>
                            <c:if test="${authUser.player && config.showPlayer}">
                                <td class="icon">
                                    <a href="#" onclick="openPlayer('${appUrl}/flashplayer/xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url=${servletUrl}/createPlaylist/auth=${cwfn:encodeUrl(auth)}/genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}/type=Xspf/playerRequest=true/${mtfn:virtualGenreName(genre)}.xspf')">
                                        <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="player" /> </a>
                                </td>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                <td class="icon">
                                    <c:choose>
                                        <c:when test="${authUser.maximumZipEntries <= 0 || genre.trackCount <= authUser.maximumZipEntries}">
                                            <a href="${servletUrl}/getZipArchive/auth=${cwfn:encodeUrl(auth)}/genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}/${mtfn:virtualGenreName(genre)}.zip">
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
                                <a href="${servletUrl}/addToPlaylist?genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">
                                    <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                            </td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:forEach>
        </table>

        <c:if test="${!empty indexPager}">
            <c:set var="pager" scope="request" value="${indexPager}" />
            <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseGenre?page=${param.page}&amp;index={index}" />
            <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
            <jsp:include page="incl_bottomPager.jsp" />
        </c:if>

    </form>

</div>

</body>

</html>
