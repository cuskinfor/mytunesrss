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
    
    <jsp:include page="/error.jsp" />
    
    <table class="start" cellspacing="0">
      <tr>
        <td rowspan="2" class="first">
          Search
          <input class="text" type="text" name="album" value="<c:out value="${param.album}"/>" style="width:140px;"/>
          <input class="button" type="submit" value="search"/>      
        </td>
        <td><a href="${servletUrl}/browseArtist" style="background-image:url('${appUrl}/images/library_small.gif');">Browse library</a></td>
      </tr>
      <tr>
        <td><a href="manager.jsp" style="background-image:url('${appUrl}/images/feeds_small.gif');">Manage Playlists</a></td>
      </tr>
    </table>
    
    <table class="select" cellspacing="0">
      <tr>
        <th colspan="3">MyTunesRSS Playlists</th>
      </tr>
      <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
        <tr class="${cwfn:choose(loopStatus.index % 2 == 0, '', 'odd')}">
          <td><c:out value="${playlist.name}" /></td>
          <td class="icon"><a href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml"><img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="RSS"/></a></td>
          <td class="icon"><a href="${servletUrl}/createM3U/playlist=${playlist.id}/mytunesrss.m3u"><img src="${appUrl}/images/m3u${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="M3U"/></a></td>
        </tr>
      </c:forEach>
    </table>
  
  </div>

</body>

</html>
