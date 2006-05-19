<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<c:if test="${!empty sessionScope.playlist}">
    <div class="playlist">
        <a class="close" href="${servletUrl}/cancelCreatePlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
            <img src="${appUrl}/images/cancel.gif" /> </a>
        <strong>New Playlist</strong> - current track count: ${sessionScope.playlist.trackCount}<a class="finish"
                                                                                                   href="${servletUrl}/editPlaylist"><img src="${appUrl}/images/finish.gif" /></a>
    </div>
</c:if>
