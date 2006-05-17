<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<c:if test="${!empty sessionScope.playlist}">
    <div class="playlist">
        Playlist: ${sessionScope.playlist.trackCount} <a href="${servletUrl}/editPlaylist">finish</a> <a class="close"
                                                                                                         href="${servletUrl}/cancelCreatePlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"><img
            src="${servletUrl}/images/playlist_close.gif" /></a>
    </div>
</c:if>
