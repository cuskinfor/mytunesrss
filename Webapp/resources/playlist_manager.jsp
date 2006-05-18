<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/showPlaylistManager</c:set>

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

    <h1 class="manager">
      <a class="portal" href="${servletUrl}/showPortal">Portal</a>
			<span>MyTunesRSS</span>
		</h1>

    <ul class="links">
        <li><a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">new playlist</a></li>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active">Playlists</th>
            <th colspan="4">Tracks</th>
        </tr>
        <tr>
            <td class="mytunes">
                Playlist 1 </td>
            <td class="tracks"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">(21&nbsp;Tracks)</a></td>
            <td class="icon">
                <a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">
                    <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
            </td>
            <td class="icon">
                <a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">
                    <img src="${appUrl}/images/edit${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
            </td>
            <td class="icon">
                <a href="${servletUrl}/createM3U/playlist=${playlist.id}/mytunesrss.m3u">
                    <img src="${appUrl}/images/delete${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="delete" /> </a>
            </td>
        </tr>
        <tr class="odd">
            <td class="itunes">
                Playlist 2 </td>
            <td class="tracks"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">(21&nbsp;Tracks)</a></td>
            <td colspan="3">&nbsp;</td>
        </tr>
    </table>

</div>

</body>

</html>
