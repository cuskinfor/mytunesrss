<%@ page contentType="application/xspf+xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb"/><?xml version="1.0" encoding="UTF-8"?>
<playlist version="1" xmlns="http://xspf.org/ns/0/">
    <creator>Codewave MyTunesRSS</creator>
    <info>http://www.codewave.de</info>
    <trackList>
        <c:forEach items="${tracks}" var="item">
            <track>
                <location>${servletUrl}/playTrack/track=${cwfn:encodeUrl(item.id)}/auth=${cwfn:encodeUrl(auth)}/${mtfn:virtualTrackName(item)}.${mtfn:suffix(item)}</location>
                <creator><c:out value="${item.artist}"/></creator>
                <album><c:out value="${item.album}"/></album>
                <title><c:out value="${item.name}"/></title>
                <c:if test="${!empty item.genre}"><annotation><c:out value="${item.genre}"/></annotation></c:if>
                <duration>${item.time}</duration>
                <image>${servletUrl}/showTrackImage/track=${cwfn:encodeUrl(item.id)}</image>
                <info>${servletUrl}/showTrackInfo/track=${cwfn:encodeUrl(item.id)}/auth=${cwfn:encodeUrl(auth)}</info>
            </track>
        </c:forEach>
    </trackList>
</playlist>
