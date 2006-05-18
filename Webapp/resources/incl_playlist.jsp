<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<c:if test="${!empty sessionScope.playlist}">
    <div class="playlist">
			<img style="position:absolute;top: 9px; left: 10px;" src="${appUrl}/images/newplaylist.gif"/> 
        <a class="close" href="${servletUrl}/cancelCreatePlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
            <img src="${appUrl}/images/cancel.gif" /> </a>
        <strong>Playlist tracks:</strong> ${sessionScope.playlist.trackCount}<a class="finish"
                                                                               href="${servletUrl}/editPlaylist"><img src="${appUrl}/images/finish.gif" /></a>
    </div>
</c:if>
