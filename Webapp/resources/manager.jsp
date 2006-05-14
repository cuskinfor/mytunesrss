<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

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
  
    <h1 class="search"><span>MyTunesRSS</span></h1>
    
    <div class="link">
      <a href="#">back to portal</a>
    </div>

    <jsp:include page="/error.jsp" />
    
    <table class="start" cellspacing="0">
      <tr>
        <td class="first" style="background-image:url('images/feeds.gif');">
          Manager:
					<a href="#">create new playlist</a>
        </td>
      </tr>
    </table>
    
    <table class="select" cellspacing="0">
      <tr>
        <th colspan="5">MyTunesRSS Playlists</th>
      </tr>
			<tr>
				<td>Playlist 1</td>
				<td class="tracks"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">(21&nbsp;Tracks)</a></td>
				<td class="icon"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml"><img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add"/></a></td>
				<td class="icon"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml"><img src="${appUrl}/images/edit${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add"/></a></td>
				<td class="icon"><a href="${servletUrl}/createM3U/playlist=${playlist.id}/mytunesrss.m3u"><img src="${appUrl}/images/delete${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="delete"/></a></td>
			</tr>
			<tr class="odd">
				<td>Playlist 2</td>
				<td class="tracks"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">(33&nbsp;Tracks)</a></td>
				<td class="icon"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml"><img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add"/></a></td>
				<td class="icon"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml"><img src="${appUrl}/images/edit${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add"/></a></td>
				<td class="icon"><a href="${servletUrl}/createM3U/playlist=${playlist.id}/mytunesrss.m3u"><img src="${appUrl}/images/delete${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="delete"/></a></td>
			</tr>
    </table>
  
  </div>

</body>

</html>
