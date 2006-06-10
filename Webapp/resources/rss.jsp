<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="text/xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>
<rss version="2.0">
    <channel>
        <title><c:out value="${channel}"/></title>
        <link>${feedUrl}</link>
        <description><fmt:message key="rssChannelDescription"/></description><c:forEach items="${tracks}" var="track"><c:set var="virtualFileName">${mtfn:virtualTrackName(track)}.${mtfn:virtualSuffix(config, track)}</c:set>
            <item>
                <title><c:out value="${track.name}"/></title>
                <description><c:if test="${!empty track.artist && !mtfn:unknown(artist)}"><c:out value="${track.artist}"/> - </c:if><c:out value="${track.album}"/></description>
                <author><c:out value="${track.artist}"/></author>
                <link>${servletUrl}/createRSS/track=${track.id}/authHash=${authHash}</link>
                <guid>${servletUrl}/createRSS/track=${track.id}</guid>
                <pubDate>${pubDate}</pubDate>
                <enclosure url="${servletUrl}/playTrack/track=${track.id}/authHash=${authHash}/${cwfn:urlEncode(virtualFileName, 'UTF-8')}"
                           type="${track.contentType}"
                           length="${track.contentLength}"/>
            </item></c:forEach>
    </channel>
</rss>
