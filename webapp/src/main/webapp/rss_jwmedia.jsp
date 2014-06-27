<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="application/rss+xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<rss version="2.0" xmlns:jwplayer="http://rss.jwpcdn.com/">
    <channel><c:forEach items="${tracks}" var="track">
        <item>
            <title><c:out value="${track.name}"/></title>
            <description><c:out value="${track.artist}"/></description>
            <guid>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt>track=${track.id}</mt:encrypt></guid>
            <c:if test="${!empty(track.imageHash)}"><jwplayer:image>${permServletUrl}/showImage/${auth}/<mt:encrypt>hash=${track.imageHash}${imgSizePathParam}</mt:encrypt></jwplayer:image></c:if>
            <jwplayer:source file="${mtfn:downloadLink(pageContext, track, null)}/${cwfn:encodeUrl(mtfn:virtualTrackName(track))}.${mtfn:suffix(config, authUser, track)}" type="${mtfn:contentType(config, authUser, track)}" />
        </item></c:forEach>
    </channel>
</rss>
