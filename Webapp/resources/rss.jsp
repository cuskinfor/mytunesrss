<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="text/xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb"/>
<rss version="2.0"<c:if test="${mediaThumbnails}"> xmlns:media="http://search.yahoo.com/mrss/"</c:if>>
    <channel>
        <title><c:out value="${channel}"/></title>
        <link>${feedUrl}</link>
        <image>
            <url>${localServletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(imageTrackId)}</mt:encrypt></url>
            <title><c:out value="${channel}"/></title>
            <link>${feedUrl}</link>
        </image>
        <description><fmt:message key="rssChannelDescription"/></description><c:forEach items="${tracks}" var="track"><c:set var="virtualFileName">${mtfn:virtualTrackName(track)}.${mtfn:suffix(track)}</c:set>
            <item>
                <title><c:out value="${track.name}"/></title>
                <description><c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" /> - <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" /></description>
                <author><c:out value="${track.artist}"/></author>
                <link>${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(track.id)}</mt:encrypt></link>
                <guid>${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(track.id)}</mt:encrypt></guid>
                <pubDate>${pubDate}</pubDate>
                <enclosure url="${servletUrl}/playTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(track.id)}</mt:encrypt>/${cwfn:encodeUrl(virtualFileName)}"
                           type="${track.contentType}"
                           length="${track.contentLength}"/>
                <c:if test="${mediaThumbnails}"><media:thumbnail url="${localServletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(track.id)}</mt:encrypt>" width="160"/></c:if>
            </item></c:forEach>
    </channel>
</rss>
