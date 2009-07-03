<%@ page contentType="application/xspf+xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><?xml version="1.0" encoding="UTF-8"?>
<playlist version="1" xmlns="http://xspf.org/ns/0/">
    <creator>Codewave MyTunesRSS</creator>
    <info>http://www.codewave.de</info>
    <trackList>
        <c:forEach items="${tracks}" var="item">
            <track>
                <location><c:out value="${mtfn:playbackLink(pageContext, item, null)}"/></location>
                <creator><c:out value="${cwfn:choose(mtfn:unknown(item.originalArtist), msgUnknown, item.originalArtist)}" /></creator>
                <album><c:out value="${cwfn:choose(mtfn:unknown(item.album), msgUnknown, item.album)}" /></album>
                <title><c:out value="${item.name}"/></title>
                <c:if test="${!empty item.genre}"><annotation><c:out value="${item.genre}"/></annotation></c:if>
                <duration>${cwfn:choose(param.jwplayer == true, item.time, item.time * 1000)}</duration>
                <c:if test="${!empty(item.imageHash)}"><image>${permServletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${cwfn:encodeUrl(item.imageHash)}/size=256</mt:encrypt></image></c:if>
                <info>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(item.id)}</mt:encrypt></info>
            </track>
        </c:forEach>
    </trackList>
</playlist>
