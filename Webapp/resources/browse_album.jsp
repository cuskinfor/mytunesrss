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
  
  <ul class="links">
    <li>
      <a href="${servletUrl}/browseArtist">by artist</a>
    </li>
    <li>
      <a href="#">new playlist</a>
    </li>
    <li style="float:right;">
      <a href="${servletUrl}/showPortal">Portal</a>
    </li>
  </ul>
  
  <form name="browse" action="" method="post">
  
    <table class="select" cellspacing="0">
      <tr>
        <th>&nbsp;</th>
        <th class="active">
          Albums
          <c:if test="${!empty param.artist}"> with "<c:out value="${param.artist}" />"</c:if>
        </th>
        <th>Artist</th>
        <th colspan="3">Tracks</th>
      </tr>
      <c:set var="backUrl">${servletUrl}/browseAlbum?artist=${param.artist}</c:set>
      <c:forEach items="${albums}" var="album" varStatus="loopStatus">
        <tr class="${cwfn:choose(loopStatus.index % 2 == 0, '', 'odd')}">
          <td class="check">
            <input type="checkbox" name="album" value="<c:out value="${album.name}"/>" />
          </td>
          <td class="artist">
            <c:out value="${album.name}" />
          </td>
          <td>
            (by&nbsp;<a href="${servletUrl}/browseArtist?album=${cwfn:urlEncode(album.name, 'UTF-8')}">${album.artistCount}&nbsp;artist${cwfn:choose(album.artistCount > 1, 's', '')}</a>)
          </td>
          <td class="tracks">
            <a href="${servletUrl}/browseTrack?album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
              (${album.trackCount})
            </a>
          </td>
          <td class="icon">
            <a href="${servletUrl}/createRSS/album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>/mytunesrss.xml">
              <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="RSS" />
            </a>
          </td>
          <td class="icon">
            <a href="${servletUrl}/createM3U/album=<c:out value="${cwfn:urlEncode(album.name, 'UTF-8')}"/>/mytunesrss.m3u">
              <img src="${appUrl}/images/m3u${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="M3U" />
            </a>
          </td>
        </tr>
      </c:forEach>
    </table>
  
    <div class="buttons">
      <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/createRSS/mytunesrss.xml'" value="RSS" />
      <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/createM3U/mytunesrss.m3u'" value="M3U" />
    </div>
  
  </form>
  
  </div>

</body>

</html>
