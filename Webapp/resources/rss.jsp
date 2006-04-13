<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>

<rss version="2.0">
    <channel>
        <title><c:out value="${channel}"/></title>
        <link>${feedUrl}</link>
        <description><fmt:message key="rss.channel.description"/></description>
        <c:forEach items="${musicFiles}" var="item">
            <item>
                <title><c:if test="{item.trackNumber != 0}">${item.textualTrackNumber} - </c:if><c:out value="${item.name}"/></title>
                <description><c:out value="${item.album}"/> - <c:out value="${item.artist}"/></description>
                <author><c:out value="${item.artist}"/></author>
                <link>${urlMap.rss}/id=${item.id}${authInfo}</link>
                <guid>${urlMap.rss}/id=${item.id}${authInfo}</guid>
                <pubDate>${pubDate}</pubDate>
                <enclosure url="${urlMap.mp3}/id=${item.id}${authInfo}/${cwfn:urlEncode(item.virtualFileName, 'UTF-8')}"
                           type="${item.contentType}"
                           length="${item.fileLength}"/>
            </item>
        </c:forEach>
    </channel>
</rss>
