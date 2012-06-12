<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="backUrl" scope="request">${servletUrl}/showPlaylistManager/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt></c:set>
<c:set var="browseArtistUrl" scope="request">${servletUrl}/browseArtist/${auth}/page=1</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script src="${appUrl}/rest.js?ts=${sessionCreationTime}" type="text/javascript"></script>

    <script type="text/javascript">

        function loadAndEditPlaylist(id) {
            PlaylistResource.startEditPaylist({
                playlist : id
            });
            document.location.href = "${servletUrl}/showResource/${auth}/<mt:encrypt key="${encryptionKey}">resource=EditPlaylist</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}";
        }

    </script>

</head>

<body class="plmanager">

    <div class="body">

        <div class="head">
            <h1 class="manager">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>

        <div class="content">

            <div class="content-inner">

                <ul class="menu">
                    <li class="first"><a id="linkStartNewPlaylist" href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${cwfn:encode64(browseArtistUrl)}"><fmt:message key="newPlaylist"/></a></li>
                    <li><a id="linkEditSmartPlayist" href="${servletUrl}/editSmartPlaylist/${auth}"><fmt:message key="newSmartPlaylist"/></a></li>
                    <li class="spacer">&nbsp;</li>
                </ul>

                <jsp:include page="/incl_error.jsp" />

                <table cellspacing="0" class="tracklist">
                    <tr>
                        <th class="active"><fmt:message key="playlists"/></th>
						<c:if test="${!empty playlists}">
							<th><fmt:message key="tracks"/></th>
							<th class="actions">&nbsp;</th>
						</c:if>
                    </tr>
                    <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
                        <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                            <td class="${fn:toLowerCase(playlist.type)}"><c:out value="${playlist.name}" /></td>
                            <td class="tracks"><a id="linkTracks${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${playlist.trackCount}</a></td>
                            <td class="actions">
                                <c:choose>
                                    <c:when test="${playlist.type == 'MyTunesSmart'}">
                                        <a id="linkEditSmart${loopStatus.index}" class="edit" href="${servletUrl}/editSmartPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlistId=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><span>Edit</span></a>
                                        </c:when>
                                    <c:otherwise>
                                        <a id="linkEdit${loopStatus.index}" class="edit" onclick="loadAndEditPlaylist('${playlist.id}')"><span>Edit</span></a>
                                    </c:otherwise>
                                </c:choose>
                                <c:choose>
                                    <c:when test="${deleteConfirmation}">
                                        <a id="deleteWithConfirmation${loopStatus.index}" class="delete" onclick="$jQ('#confirmDeletePlaylist').data('serverCall', '${servletUrl}/deletePlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>');$jQ('#playlistName').text('${mtfn:escapeJs(playlist.name)}');openDialog('#confirmDeletePlaylist')"><span>Delete</span></a>
                                    </c:when>
                                    <c:otherwise>
                                        <a id="deleteWithoutConfirmation${loopStatus.index}" class="delete" href="${servletUrl}/deletePlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>"><span>Delete</span></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty playlists}">
                        <tr><td colspan="3" class="empty"><fmt:message key="noPlaylists"/></td></tr>
                    </c:if>
                </table>

                <c:if test="${!empty pager}">
                    <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPlaylistManager/${auth}/index={index}" />
                    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
                    <jsp:include page="incl_bottomPager.jsp" />
                </c:if>

            </div>

        </div>

        <div class="footer">
            <div class="inner"></div>
        </div>

    </div>

    <div id="confirmDeletePlaylist" class="dialog">
        <h2>
            <fmt:message key="confirmDeletePlaylistTitle"/>
        </h2>
        <div>
            <p>
                <fmt:message key="dialog.confirmDeletePlaylist"><fmt:param><span id="playlistName"></span></fmt:param></fmt:message>
            </p>
            <p align="right">
                <button id="linkConfirmDelPlaylistNo" onclick="$jQ.modal.close()"><fmt:message key="no"/></button>
                <button id="linkConfirmDelPlaylistYes" onclick="document.location.href = $jQ('#confirmDeletePlaylist').data('serverCall')"><fmt:message key="yes"/></button>
            </p>
        </div>
    </div>

</body>

</html>
