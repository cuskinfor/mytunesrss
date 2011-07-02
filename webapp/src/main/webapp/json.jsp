<%@ page contentType="application/json;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %><%--@elvariable id="tracks" type="java.util.List<de.codewave.mytunesrss.datastore.statement.Track>"--%>

[
<c:forEach items="${tracks}" var="item" varStatus="loopStatus">
    {
        "location" : "${mtfn:escapeJson(mtfn:playbackLink(pageContext, item, null))}",
        "artist" : "${mtfn:escapeJson(cwfn:choose(mtfn:unknown(item.originalArtist), msgUnknownArtist, item.originalArtist))}",
        "album" : "${mtfn:escapeJson(cwfn:choose(mtfn:unknown(item.album), msgUnknownAlbum, item.album))}",
        "name" : "${mtfn:escapeJson(item.name)}",
        "contentLength" : ${item.contentLength},
        <c:if test="${!empty item.mediaType}">"mediaType" : "${mtfn:escapeJson(item.mediaType)}",</c:if>
        <c:if test="${!empty item.contentType}">"contentType" : "${mtfn:escapeJson(item.contentType)}",</c:if>
        <c:if test="${!empty item.comment}">"comment" : "${mtfn:escapeJson(item.comment)}",</c:if>
        <c:if test="${!empty item.genre}">"genre" : "${mtfn:escapeJson(item.genre)}",</c:if>
        <c:if test="${item.time > 0}">"duration" : ${item.time * timefactor},</c:if>
        <c:if test="${!empty(item.imageHash)}">"image" : "<mt:escapeJson>${permServletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${item.imageHash}</mt:encrypt></mt:escapeJson>",</c:if>
        "info" : "<mt:escapeJson>${permServletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(item.id)}</mt:encrypt></mt:escapeJson>"
    }<c:if test="${!loopStatus.last}">,</c:if>
</c:forEach>
]
