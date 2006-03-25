<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<rss version="2.0">
    <channel>
        <title>
            <c:out value="${channel}"/>
        </title>
        <link>${servletUrl}?method=getRssFeed&amp;channel=${cwfn:urlEncode(channel, 'UTF-8')}
            <c:forEach items="${musicFiles}" var="file">&amp;id=${file.id}</c:forEach>
        </link>
        <description>Codewave MyTunesRSS v${cwfn:sysprop('mytunesrss.version')}</description>
        <c:forEach items="${musicFiles}" var="item">
            <item>
                <title>
                    <c:if test="{item.trackNumber != 0}">${item.textualTrackNumber} -</c:if>
                    <c:out value="${item.name}"/>
                </title>
                <description>
                    <c:out value="${item.album}"/>
                    -
                    <c:out value="${item.artist}"/>
                </description>
                <link>${servletUrl}/${item.id}</link>
                <guid>${servletUrl}/${item.id}</guid>
                <pubDate>${pubDate}</pubDate>
                <enclosure url="${servletUrl}/${item.id}/${cwfn:urlEncode(item.virtualFileName, 'UTF-8')}"
                           type="audio/mp3"
                           length="${item.fileLength}"/>
            </item>
        </c:forEach>
    </channel>
</rss>
