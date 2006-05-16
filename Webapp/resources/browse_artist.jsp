<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

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

    <h1 class="browse"><span>MyTunesRSS</span></h1>

    <jsp:include page="/error.jsp" />

    <ul class="links">
        <li>
            <a href="${servletUrl}/showPortal">back to portal</a>
        </li>
        <li>
            <a href="${servletUrl}/startNewPlaylist">new playlist</a>
        </li>
        <li style="float:right;">
            <a href="${servletUrl}/browseAlbum">sort by album</a>
        </li>
    </ul>

    <c:if test="${!empty sessionScope.playlist}">
        <ul class="links">
            <li>
                Playlist: ${sessionScope.playlist.trackCount}
            </li>
            <li>
                <a href="${servletUrl}/editPlaylist">finish</a>
            </li>
            <li style="float:right;">
                <a href="${servletUrl}/cancelCreatePlaylist">cancel</a>
            </li>
        </ul>
    </c:if>

    <form name="browse" action="" method="post">

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
            <c:set var="backUrl">${servletUrl}/browseArtist?album=${param.album}</c:set>
            <c:forEach items="${artists}" var="artist" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, '', 'odd')}">
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
                            <td class="icon">
                                <a href="${servletUrl}/createRSS/artist=<c:out value="${cwfn:urlEncode(artist.name, 'UTF-8')}"/>/mytunesrss.xml"> <img
                                        src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                        alt="RSS" /> </a>
                            </td>
                            <td class="icon">
                                <a href="${servletUrl}/createM3U/artist=<c:out value="${cwfn:urlEncode(artist.name, 'UTF-8')}"/>/mytunesrss.m3u"> <img
                                        src="${appUrl}/images/m3u${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                        alt="M3U" /> </a>
                            </td>
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

        <c:if test="${!empty sessionScope.playlist}">
            <div class="buttons">
                <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/addToPlaylist'" value="add selected" />
            </div>
        </c:if>

    </form>

</div>

</body>

</html>
