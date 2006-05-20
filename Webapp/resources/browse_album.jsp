<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum?artist=${param.artist}&page=${param.page}&index=${param.index}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="title" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
    <![endif]-->

</head>

<body>

<div class="body">

<h1 class="browse">
    <a class="portal" href="${servletUrl}/showPortal">Portal</a> <span>MyTunesRSS</span>
</h1>

<jsp:include page="/incl_error.jsp" />

<ul class="links">
    <li>
        <a href="${servletUrl}/browseArtist?page=${cwfn:choose(empty param.page && empty param.artist, '', pagerInitialPage)}">browse artists</a>
    </li>
    <c:if test="${empty sessionScope.playlist}">
        <li>
            <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">new playlist</a>
        </li>
    </c:if>
</ul>

<jsp:include page="incl_playlist.jsp" />

<c:set var="pager" scope="request" value="${albumPager}" />
<c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum?page={index}" />
<c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.artist, '*', param.page)}" />
<jsp:include page="incl_pager.jsp" />

<form name="browse" action="" method="post">
    <input type="hidden" name="backUrl" value="${backUrl}" />

    <table class="select" cellspacing="0">
        <tr>
            <c:if test="${!empty sessionScope.playlist}"><th>&nbsp;</th></c:if>
            <th class="active">
                Albums
                <c:if test="${!empty param.artist}"> with "<c:out value="${param.artist}" />"</c:if>
            </th>
            <th>Artist</th>
            <th colspan="3">Tracks</th>
        </tr>
        <c:forEach items="${albums}" var="album" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 1, '', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">
                        <input type="checkbox" name="album" value="<c:out value="${album.name}"/>" />
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
                                    <a href="${servletUrl}/browseAlbum?artist=${cwfn:urlEncode(album.artist, 'UTF-8')}">
                                        <c:out value="${cwfn:choose(mtfn:unknown(album.artist), '(unknown)', album.artist)}" /></a>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>various</c:otherwise>
                    </c:choose>

                </td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack?album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"> ${album.trackCount} </a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:forEach items="${config.feedTypes}" var="feedType">
                            <td class="icon">
                                <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>/${mtfn:virtualAlbumName(album)}.${config.feedFileSuffix[feedType]}">
                                    <img src="${appUrl}/images/${feedType}${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="${feedType}" /> </a>
                            </td>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist?album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                                <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:forEach>
        <c:if test="${singleArtist && fn:length(albums) > 1}">
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 1, '', 'odd')}">
                <c:if test="${!empty sessionScope.playlist}">
                    <td class="check">&nbsp;</td>
                </c:if>
                <td colspan="2"><em>All tracks of the above albums</em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack?artist=<c:out value="${cwfn:urlEncode(param.artist, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">${singleArtistTrackCount}</a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:forEach items="${config.feedTypes}" var="feedType">
                            <td class="icon">
                                <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/artist=<c:out value="${cwfn:urlEncode(param.artist, 'UTF-8')}"/>/${mtfn:cleanFileName(param.artist)}.${config.feedFileSuffix[feedType]}">
                                    <img src="${appUrl}/images/${feedType}${cwfn:choose(fn:length(albums) % 2 == 1, '', '_odd')}.gif"
                                         alt="${feedType}" /> </a>
                            </td>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist?artist=<c:out value="${cwfn:urlEncode(param.artist, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 1, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum?page=${param.page}&artist=${param.album}&index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

    <c:if test="${!empty sessionScope.playlist}">
        <div class="buttons">
            <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/addToPlaylist'" value="add selected" />
        </div>
    </c:if>

</form>

</div>

</body>

</html>
