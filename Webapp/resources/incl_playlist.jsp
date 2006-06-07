<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<fmt:message var="newPlaylistName" key="newPlaylistName" />

<c:if test="${!empty sessionScope.playlist}">
    <div class="playlist">
        <a class="close" href="${servletUrl}/cancelCreatePlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"><img src="${appUrl}/images/cancel.gif" /></a>
				<a class="finish" href="${servletUrl}/editPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}"><img src="${appUrl}/images/finish.gif" /></a>
				<span>
					<strong>${cwfn:choose (empty sessionScope.playlist.name, newPlaylistName, sessionScope.playlist.name)}</strong>
					- <fmt:message key="playlistTrackCount" /> : ${sessionScope.playlist.trackCount}
				</span>
    </div>
</c:if>
