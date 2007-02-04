<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseGenre?page=${param.page}&amp;index=${param.index}</c:set>

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
                <th colspan="${cwfn:choose(config.showDownload, 2, 1) + config.feedTypeCount}"><fmt:message key="tracks"/></th>
            </tr>
            <c:forEach items="${genres}" var="genre" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td class="genre">
                        <c:out value="${genre.name}" />
                    </td>
                    <td class="album">
                        <a href="${servletUrl}/browseAlbum?genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}"> ${genre.albumCount} </a>
                    </td>
                    <td class="genreartist">
                        <a href="${servletUrl}/browseArtist?genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}"> ${genre.artistCount} </a>
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
                            <c:if test="${authUser.m3u && config.showM3u}">
                                <td class="icon">
                                    <a href="${servletUrl}/createM3U/auth=${cwfn:encodeUrl(auth)}/genre=${cwfn:encodeUrl(cwfn:encode64(genre.name))}/${mtfn:virtualGenreName(genre)}.m3u">
                                        <img src="${appUrl}/images/m3u${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="m3u" /> </a>
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
