<%@ page contentType="application/xspf+xml;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><?xml version="1.0" encoding="UTF-8"?>
<playlist version="1" xmlns="http://xspf.org/ns/0/">
    <creator>Codewave MyTunesRSS</creator>
    <info>http://www.codewave.de</info>
    <trackList>
        <c:forEach items="${tracks}" var="item">
            <track>
                <location><c:out value="${mtfn:playbackLink(pageContext, item, null)}"/>/${cwfn:encodeUrl(mtfn:virtualTrackName(item))}.${mtfn:suffix(config, authUser, item)}</location>
                <creator><c:out value="${cwfn:choose(mtfn:unknown(item.artist), msgUnknownArtist, item.artist)}" /></creator>
                <album><c:out value="${cwfn:choose(mtfn:unknown(item.album), msgUnknownAlbum, item.album)}" /></album>
                <title><c:out value="${item.name}"/></title>
                <c:if test="${!empty item.genre}"><annotation><c:out value="${item.genre}"/></annotation></c:if>
                <c:if test="${item.time > 0}"><duration>${item.time * timefactor}</duration></c:if>
                <c:if test="${!empty(item.imageHash)}"><image>${permServletUrl}/showImage/${auth}/<mt:encrypt>hash=${item.imageHash}${imgSizePathParam}</mt:encrypt></image></c:if>
                <info>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt>track=${cwfn:encodeUrl(item.id)}</mt:encrypt></info>
            </track>
        </c:forEach>
    </trackList>
</playlist>
