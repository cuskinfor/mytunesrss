<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:message var="newPlaylistName" key="newPlaylistName" />

<c:if test="${!empty sessionScope.playlist}">
    <div class="playlisttop"></div>
    <div class="playlist">
        <a class="close" href="${servletUrl}/cancelCreatePlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><img src="${appUrl}/images/cancel.gif" alt=""/></a>
				<a class="finish" href="${servletUrl}/editPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><img src="${appUrl}/images/finish.gif" alt=""/></a>
				<span>
					<strong>${cwfn:choose (empty sessionScope.playlist.name, newPlaylistName, sessionScope.playlist.name)}</strong>
					- <fmt:message key="playlistTrackCount" /> : ${sessionScope.playlist.trackCount}
				</span>
    </div>
    <div class="playlistbottom"></div>
</c:if>
