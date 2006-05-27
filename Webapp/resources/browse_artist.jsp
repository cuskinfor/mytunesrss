<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/browseArtist?album=${param.album}&page=${param.page}&index=${param.index}</c:set>

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
            <a href="${servletUrl}/browseAlbum?page=${cwfn:choose(empty param.page && empty param.album, '', '1')}">browse albums</a>
        </li>
        <c:if test="${empty sessionScope.playlist}">
            <li>
                <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">new playlist</a>
            </li>
        </c:if>
    </ul>

    <jsp:include page="incl_playlist.jsp" />

    <c:set var="pager" scope="request" value="${artistPager}" />
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseArtist?page={index}" />
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.album, '*', param.page)}" />
    <jsp:include page="incl_pager.jsp" />

    <form id="browse" action="" method="post">
        <input type="hidden" name="backUrl" value="${backUrl}" />

        <table class="select" cellspacing="0">
            <tr>
                <c:if test="${!empty sessionScope.playlist}"><th>&nbsp;</th></c:if>
                <th class="active">
                    Artists
                    <c:if test="${!empty param.album}"> on "<c:out value="${param.album}" />"</c:if>
                </th>
                <th>Album</th>
                <th colspan="3">Tracks</th>
            </tr>
            <c:forEach items="${artists}" var="artist" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <c:if test="${!empty sessionScope.playlist}">
                        <td class="check"><input type="checkbox" name="artist" value="<c:out value="${artist.name}"/>" /></td>
                    </c:if>
                    <td class="artist">
                        <c:out value="${cwfn:choose(mtfn:unknown(artist.name), '(unknown)', artist.name)}" />
                    </td>
                    <td class="album">
                        <a href="${servletUrl}/browseAlbum?artist=<c:out value="${cwfn:urlEncode(artist.name, 'UTF-8')}"/>"> ${artist.albumCount} </a>
                    </td>
                    <td class="tracks">
                        <a href="${servletUrl}/browseTrack?artist=<c:out value="${cwfn:urlEncode(artist.name, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"> ${artist.trackCount} </a>
                    </td>
                    <c:choose>
                        <c:when test="${empty sessionScope.playlist}">
                            <c:forEach items="${config.feedTypes}" var="feedType">
                                <td class="icon">
                                    <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/artist=<c:out value="${cwfn:urlEncode(artist.name, 'UTF-8')}"/>/${mtfn:virtualArtistName(artist)}.${config.feedFileSuffix[feedType]}">
                                        <img src="${appUrl}/images/${feedType}${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                             alt="${feedType}" /> </a>
                                </td>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <td class="icon">
                                <a href="${servletUrl}/addToPlaylist?artist=<c:out value="${cwfn:urlEncode(artist.name, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                                    <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                            </td>
                        </c:otherwise>
                    </c:choose>
                </tr>
            </c:forEach>
        </table>

        <c:if test="${!empty indexPager}">
            <c:set var="pager" scope="request" value="${indexPager}" />
            <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseArtist?page=${param.page}&album=${param.album}&index={index}" />
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
