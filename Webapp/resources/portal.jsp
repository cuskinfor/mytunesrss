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
      <link rel="stylesheet" type="text/css" href="styles/ie.css" />
    <![endif]-->

</head>

<body>

<div class="body">

    <h1 class="search"><span>MyTunesRSS</span></h1>

    <jsp:include page="/error.jsp" />

    <table class="start" cellspacing="0">
        <tr>
            <td>&nbsp;</td>
            <td><a href="#" style="background-image:url('${appUrl}/images/search.gif');">Search</a></td>
            <td><a href="${servletUrl}/browseArtist" style="background-image:url('${appUrl}/images/library.gif');">Browse library</a></td>
            <td><a href="#" style="background-image:url('${appUrl}/images/feeds.gif');">Manage Feeds</a></td>
            <td>&nbsp;</td>
        </tr>
    </table>

    <table class="select" cellspacing="0">
        <tr>
            <th>&nbsp;</th>
            <th colspan="3">MyTunesRSS Playlists</th>
        </tr>
        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, '', 'odd')}">
                <td class="check"><input type="checkbox" /></td>
                <td><c:out value="${playlist.name}" /></td>
                <td class="icon"><a class="rss" href="${servletUrl}/createRSS?playlist=${playlist.id}/mytunesrss.xml">&nbsp;</a></td>
                <td class="icon"><a class="m3u" href="${servletUrl}/createM3U/playlist=${playlist.id}/mytunesrss.m3u">&nbsp;</a></td>
            </tr>
        </c:forEach>
    </table>

    <div class="buttons">
        <input type="submit" value="RSS" />
        <input type="submit" value="M3U" />
    </div>

</div>

</body>

</html>
