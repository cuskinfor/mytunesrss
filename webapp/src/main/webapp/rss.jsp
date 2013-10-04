<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="application/rss+xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:atom="http://www.w3.org/2005/Atom">
    <channel>
        <title><c:out value="${channel}"/></title>
        <link>${feedUrl}</link>
        <atom:link href="${feedUrl}" rel="self" type="application/rss+xml" />
        <c:if test="${!empty imageTrackId}">
            <image>
                <url>${permServletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${imageHash}</mt:encrypt></url>
                <title><c:out value="${channel}"/></title>
                <link>${feedUrl}</link>
            </image>
        </c:if>
        <description><c:out value="${globalConfig.rssDescription}"/></description><c:forEach items="${tracks}" var="track">
            <item>
                <title><c:out value="${track.name}"/></title>
                <description><c:if test="${!empty(track.imageHash)}">&lt;img src="${permServletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=128</mt:encrypt>" /&gt;&lt;br/&gt;</c:if><c:out value="${cwfn:choose(mtfn:unknown(track.originalArtist), msgUnknownArtist, track.originalArtist)}" /> - <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknownAlbum, track.album)}" /></description>
                <dc:creator><c:out value="${track.originalArtist}"/></dc:creator>
                <link>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt></link>
                <guid>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt></guid>
                <pubDate>${mtfn:rssDate(track['tsUpdated'])}</pubDate>
                <enclosure url="<c:out value="${mtfn:downloadLink(pageContext, track, null)}"/>/${cwfn:encodeUrl(mtfn:virtualTrackName(track))}.${mtfn:suffix(config, authUser, track)}"
                           type="${mtfn:contentType(config, authUser, track)}"
                           <c:if test="${!mtfn:transcoding(pageContext, authUser, track)}">length="${track.contentLength}"</c:if>
                        />
            </item></c:forEach>
    </channel>
</rss>
