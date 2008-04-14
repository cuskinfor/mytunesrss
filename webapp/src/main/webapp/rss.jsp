<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="text/xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<rss version="2.0"<c:if test="${userAgentPsp}"> xmlns:media="http://search.yahoo.com/mrss/"</c:if>>
    <channel>
        <title><c:out value="${channel}"/></title>
        <link>${permServletUrl}</link>
        <image>
            <url>${permServletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(imageTrackId)}</mt:encrypt></url>
            <title><c:out value="${channel}"/></title>
            <c:if test="${!userAgentPsp}"><link>${permServletUrl}</link></c:if>
        </image>
        <description><fmt:message key="rssChannelDescription"/></description><c:forEach items="${tracks}" var="track"><c:set var="virtualFileName">${mtfn:virtualTrackName(track)}.${mtfn:suffix(config, authUser, track)}</c:set>
            <item>
                <title><c:out value="${track.name}"/></title>
                <description><c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" /> - <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" /></description>
                <author><c:out value="${track.artist}"/></author>
                <link>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt></link>
                <guid>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt></guid>
                <pubDate>${pubDate}</pubDate>
                <enclosure url="${permServletUrl}/playTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/tc=${mtfn:tcParamValue(config, authUser, track)}</mt:encrypt>/${cwfn:encodeUrl(virtualFileName)}"
                           type="${track.contentType}"
                           length="${track.contentLength}"/>
                <c:if test="${userAgentPsp}"><media:thumbnail url="${permServletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>" width="160"/></c:if>
            </item></c:forEach>
    </channel>
</rss>
