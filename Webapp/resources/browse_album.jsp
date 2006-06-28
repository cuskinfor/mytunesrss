<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum?artist=${cwfn:urlEncode(param.artist, 'UTF-8')}&amp;page=${param.page}&amp;index=${param.index}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="applicationTitle" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
    <![endif]-->

</head>

<body>

<div class="body">

<h1 class="browse">
    <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
</h1>

<jsp:include page="/incl_error.jsp" />

<ul class="links">
    <li>
        <a href="${servletUrl}/browseArtist?page=${cwfn:choose(empty param.page && empty param.artist, '', '1')}"><fmt:message key="browseArtist"/></a>
    </li>
    <c:if test="${empty sessionScope.playlist}">
        <li>
            <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"><fmt:message key="newPlaylist"/></a>
        </li>
    </c:if>
</ul>

<jsp:include page="incl_playlist.jsp" />

<c:set var="pager" scope="request" value="${albumPager}" />
<c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum?page={index}" />
<c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.artist, '*', param.page)}" />
<jsp:include page="incl_pager.jsp" />

<form id="browse" action="" method="post">

	<fieldset>
    <input type="hidden" name="backUrl" value="${backUrl}" />
	</fieldset>

    <table class="select" cellspacing="0">
        <tr>
            <c:if test="${!empty sessionScope.playlist}"><th>&nbsp;</th></c:if>
            <th class="active">
                <fmt:message key="albums"/>
                <c:if test="${!empty param.artist}"> <fmt:message key="with"/> "<c:out value="${param.artist}" />"</c:if>
            </th>
            <th><fmt:message key="artist"/></th>
            <th colspan="${cwfn:choose(config.showDownload, 2, 1) + fn:length(config.feedTypes)}"><fmt:message key="tracks"/></th>
        </tr>
        <c:forEach items="${albums}" var="album" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
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
                        <c:otherwise><fmt:message key="variousArtists"/></c:otherwise>
                    </c:choose>

                </td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack?album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>&amp;backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"> ${album.trackCount} </a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:forEach items="${config.feedTypes}" var="feedType">
                            <td class="icon">
                                <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/authHash=${authHash}/album=<c:out value="${mtfn:hex(album.name)}"/>/${mtfn:virtualAlbumName(album)}.${config.feedFileSuffix[feedType]}">
                                    <img src="${appUrl}/images/${feedType}${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="${feedType}" /> </a>
                            </td>
                        </c:forEach>
                        <c:if test="${config.showDownload}">
                            <td class="icon">
                                <a href="${servletUrl}/getZipArchive/authHash=${authHash}/album=<c:out value="${mtfn:hex(album.name)}"/>/${mtfn:virtualAlbumName(album)}.zip">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                            </td>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist?album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>&amp;backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
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
                    <a href="${servletUrl}/browseTrack?fullAlbums=true&amp;artist=<c:out value="${cwfn:urlEncode(param.artist, 'UTF-8')}"/>&amp;backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">${singleArtistTrackCount}</a>
                </td>
                <c:choose>
                    <c:when test="${empty sessionScope.playlist}">
                        <c:forEach items="${config.feedTypes}" var="feedType">
                            <td class="icon">
                                <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/authHash=${authHash}/fullAlbums=true/artist=<c:out value="${mtfn:hex(param.artist)}"/>/${mtfn:cleanFileName(param.artist)}.${config.feedFileSuffix[feedType]}">
                                    <img src="${appUrl}/images/${feedType}${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif"
                                         alt="${feedType}" /> </a>
                            </td>
                        </c:forEach>
                        <c:if test="${config.showDownload}">
                            <td class="icon">
                                <a href="${servletUrl}/getZipArchive/authHash=${authHash}/artist=<c:out value="${mtfn:hex(param.artist)}"/>/${mtfn:cleanFileName(param.artist)}.zip">
                                    <img src="${appUrl}/images/download${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="download"/>" /></a>
                            </td>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <td class="icon">
                            <a href="${servletUrl}/addToPlaylist?fullAlbums=true&amp;artist=<c:out value="${cwfn:urlEncode(param.artist, 'UTF-8')}"/>&amp;backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                        </td>
                    </c:otherwise>
                </c:choose>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum?page=${param.page}&amp;artist=${cwfn:urlEncode(param.album, 'UTF-8')}&amp;index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

    <c:if test="${!empty sessionScope.playlist}">
        <div class="buttons">
            <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/addToPlaylist'" value="<fmt:message key="addSelected"/>" />
        </div>
    </c:if>

</form>

</div>

</body>

</html>
