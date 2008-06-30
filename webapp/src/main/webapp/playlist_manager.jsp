<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl" scope="request">${servletUrl}/showPlaylistManager/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt></c:set>
<c:set var="browseArtistUrl" scope="request">${servletUrl}/browseArtist/${auth}/page=1</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

<div class="body">

    <h1 class="manager">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <ul class="links">
        <li><a href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${cwfn:encode64(browseArtistUrl)}"><fmt:message key="newPlaylist"/></a></li>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active"><fmt:message key="playlists"/></th>
						<c:if test="${!empty playlists}">
							<th colspan="4"><fmt:message key="tracks"/></th>
						</c:if>
        </tr>
        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td class="mytunes"><c:out value="${playlist.name}" /></td>
                <td class="tracks"><a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${playlist.trackCount}</a></td>
                <td class="icon">
                    <a href="${servletUrl}/loadAndEditPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=true/playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                        <img src="${appUrl}/images/edit${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                </td>
                <td class="icon">
                    <c:choose>
                        <c:when test="${deleteConfirmation}">
                            <a href="#" onclick="showDialog('confirmDeletePlaylist', [function() {document.location.href='${servletUrl}/deletePlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>}'}, null])">
                                <img src="${appUrl}/images/delete${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="delete" /> </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${servletUrl}/deletePlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>">
                                <img src="${appUrl}/images/delete${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="delete" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
			<c:if test="${empty playlists}">
				<tr><td><em><fmt:message key="noPlaylists"/></em></td></tr>
			</c:if>
    </table>

    <c:if test="${!empty pager}">
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPlaylistManager/${auth}/index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

<div id="glasspane" class="glasspane">
</div>

<div id="confirmDeletePlaylist" class="dialogbox">
    <div class="dialogMessage">
        <fmt:message key="dialog.confirmDeletePlaylist"/>
    </div>
    <div class="dialogButton">
        <input type="button" onclick="clickDialog(1)" value="<fmt:message key="no" />" />
    </div>
    <div class="dialogButton">
        <input type="button" onclick="clickDialog(0)" value="<fmt:message key="yes" />" />
    </div>
</div>

</body>

</html>
