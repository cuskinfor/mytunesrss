<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseArtist?album=${cwfn:encodeUrl(param.album)}&amp;page=${param.page}&amp;index=${param.index}</c:set>

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
            <a href="${servletUrl}/browseAlbum?page=${cwfn:choose(empty param.page && empty param.album, '', '1')}"><fmt:message key="browseAlbums"/></a>
        </li>
        <c:if test="${empty sessionScope.playlist}">
            <li>
                <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:encodeUrl(backUrl)}"><fmt:message key="newPlaylist"/></a>
            </li>
        </c:if>
    </ul>

    <jsp:include page="incl_playlist.jsp" />

    <c:set var="pager" scope="request" value="${artistPager}" />
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseArtist?page={index}" />
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.album, '*', param.page)}" />
    <jsp:include page="incl_pager.jsp" />

    <form id="browse" action="" method="post">

			<fieldset>
        <input type="hidden" name="backUrl" value="${backUrl}" />
			</fieldset>

        <table class="select" cellspacing="0">
            <tr>
                <c:if test="${!empty sessionScope.playlist}">
                    <th class="check"><input type="checkbox" name="none" value="none" onclick="selectAllByLoop('artist', 1, ${fn:length(artists)}, this)" /></th>
                </c:if>
                <th class="active">
                    <fmt:message key="artists"/>
                    <c:if test="${!empty param.album}"> on "<c:out value="${param.album}" />"</c:if>
                </th>
                <th><fmt:message key="albums"/></th>
                <th colspan="${cwfn:choose(config.showDownload, 2, 1) + config.feedTypeCount}"><fmt:message key="tracks"/></th>
            </tr>
            <c:forEach items="${artists}" var="artist" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <c:if test="${!empty sessionScope.playlist}">
                        <td class="check"><input type="checkbox" name="artist" id="artist${loopStatus.count}" value="${cwfn:encode64(artist.name)}" /></td>
                    </c:if>
                    <td class="artist">
                        <c:out value="${cwfn:choose(mtfn:unknown(artist.name), '(unknown)', artist.name)}" />
                    </td>
                    <td class="album">
                        <a href="${servletUrl}/browseAlbum?artist=${cwfn:encodeUrl(cwfn:encode64(artist.name))}"> ${artist.albumCount} </a>
                    </td>
                    <td class="tracks">
                        <a href="${servletUrl}/browseTrack?artist=${cwfn:encodeUrl(cwfn:encode64(artist.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}"> ${artist.trackCount} </a>
                    </td>
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <c:if test="${authUser.rss && config.showRss}">
                                <td class="icon">
                                    <a href="${servletUrl}/createRSS/auth=${cwfn:encodeUrl(auth)}/artist=${cwfn:encodeUrl(cwfn:encode64(artist.name))}/${mtfn:virtualArtistName(artist)}.xml">
                                        <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="rss" /> </a>
                                </td>
                            </c:if>
                            <c:if test="${authUser.m3u && config.showM3u}">
                                <td class="icon">
                                    <a href="${servletUrl}/createM3U/auth=${cwfn:encodeUrl(auth)}/artist=${cwfn:encodeUrl(cwfn:encode64(artist.name))}/${mtfn:virtualArtistName(artist)}.m3u">
                                        <img src="${appUrl}/images/m3u${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="m3u" /> </a>
                                </td>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                <td class="icon">
                                    <a href="${servletUrl}/getZipArchive/auth=${cwfn:encodeUrl(auth)}/artist=${cwfn:encodeUrl(cwfn:encode64(artist.name))}/${mtfn:virtualArtistName(artist)}.zip">
                                        <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                                </td>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <td class="icon">
                                <a href="${servletUrl}/addToPlaylist?artist=${cwfn:encodeUrl(cwfn:encode64(artist.name))}&amp;backUrl=${cwfn:encodeUrl(backUrl)}">
                                    <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                            </td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:forEach>
        </table>

        <c:if test="${!empty indexPager}">
            <c:set var="pager" scope="request" value="${indexPager}" />
            <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseArtist?page=${param.page}&amp;album=${cwfn:encodeUrl(param.album)}&amp;index={index}" />
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
