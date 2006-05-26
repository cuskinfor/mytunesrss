<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<c:if test="${!empty sessionScope.playlist}">
    <div class="playlist">
        <a class="close" href="${servletUrl}/cancelCreatePlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
					<img src="${appUrl}/images/cancel.gif" />
				</a>
				<a class="finish" href="${servletUrl}/editPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
					<img src="${appUrl}/images/finish.gif" />
				</a>
				<span>
					<strong>${cwfn:choose (empty sessionScope.playlist.name, 'New Playlist', sessionScope.playlist.name)}</strong>
					- current track count: ${sessionScope.playlist.trackCount}
				</span>
    </div>
</c:if>
