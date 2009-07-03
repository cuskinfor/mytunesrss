<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="application/rss+xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<rss version="2.0"<c:if test="${userAgent != 'Psp'}"> xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom"</c:if> xmlns:media="http://search.yahoo.com/mrss/">
    <channel>
        <title><c:out value="${channel}"/></title>
        <link>${feedUrl}</link>
        <c:if test="${userAgent != 'Psp'}"><atom:link href="${feedUrl}" rel="self" type="application/rss+xml" /></c:if>
        <c:if test="${!empty imageTrackId}">
            <image>
                <url>${permServletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${cwfn:encodeUrl(imageHash)}</mt:encrypt></url>
                <title><c:out value="${channel}"/></title>
                <c:if test="${userAgent != 'Psp'}"><link>${feedUrl}</link></c:if>
            </image>
        </c:if>
        <description><fmt:message key="rssChannelDescription"/></description><c:forEach items="${tracks}" var="track"><c:set var="virtualFileName">${mtfn:virtualTrackName(track)}.${mtfn:suffix(config, authUser, track)}</c:set>
            <item>
                <title><c:out value="${track.name}"/></title>
                <description><c:out value="${cwfn:choose(mtfn:unknown(track.originalArtist), msgUnknown, track.originalArtist)}" /> - <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" /></description>
                <c:choose><c:when test="${userAgent == 'Psp'}"><author><c:out value="${track.originalArtist}"/></author></c:when><c:otherwise><dc:creator><c:out value="${track.originalArtist}"/></dc:creator></c:otherwise></c:choose>
                <link>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt></link>
                <guid>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt></guid>
                <pubDate>${mtfn:rssDate(track.tsUpdated)}</pubDate>
                <enclosure url="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>"
                           type="${mtfn:contentType(config, authUser, track)}"
                           <c:if test="${!mtfn:transcoding(pageContext, authUser, track)}">length="${track.contentLength}"</c:if>
                        />
                <c:if test="${!empty(track.imageHash)}"><media:thumbnail url="${permServletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=256</mt:encrypt>"/></c:if>
            </item></c:forEach>
    </channel>
</rss>
