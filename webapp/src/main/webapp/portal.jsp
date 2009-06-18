<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl" scope="request">${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${playlists}" var="playlist">
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${playlist.name}" />" />
        </c:forEach>
    </c:if>

    <script type="text/javascript">
      $jQ(document).ready(function(){
        $jQ("#fuzziness").slider({value:${config.searchFuzziness}});
      });
    </script>

</head>

<body>

<div class="body">

    <h1 class="search" onclick="window.open('http://www.codewave.de')" style="cursor: pointer"><span><fmt:message key="myTunesRss" /></span></h1>

    <ul class="links">
        <c:if test="${authUser.editWebSettings}">
            <li><a href="${servletUrl}/showSettings/${auth}">
                <fmt:message key="doSettings" />
            </a></li>
        </c:if>
        <li><a href="${servletUrl}/browseServers/${auth}">
            <fmt:message key="browseServers" />
        </a></li>
        <li style="float:right"><a href="${servletUrl}/logout">
            <fmt:message key="doLogout" />
        </a></li>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <form id="search" action="${servletUrl}/searchTracks/${auth}" method="post" onsubmit="$('inputFuzziness').value=$jQ('#fuzziness').slider('option', 'value')">

        <table class="portal" cellspacing="0">
            <tr>
                <td class="search">
                    <table border="0" cellspacing="0" cellpadding="0" style="border-bottom:0"><tr><td width="100%" style="background:transparent;padding:0 0"><input class="text" type="text" name="searchTerm" value="<c:out value="${lastSearchTerm}"/>" style="width:90%"/></td><td style="background:transparent;padding:0 0"><input class="button" type="submit" value="<fmt:message key="doSearch"/>"/></td></tr></table>
                    <table border="0" cellspacing="0" cellpadding="0" style="border-bottom:0"><tr><td style="background:transparent;padding:0 0"><div class="searchFuzzinessLabel"><fmt:message key="search.fuzziness"/>:</div></td><td width="100%" style="background:transparent;padding:0 0"><div class="searchFuzzinessSlider" id="fuzziness" /></td></tr></table>
                    <div>
                        <input id="inputFuzziness" type="hidden" name="searchFuzziness" />
                        <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
                    </div>
                </td>
                <td class="links">
                    <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>" style="background-image:url('${appUrl}/images/library_small.gif');">
                        <fmt:message key="browseLibrary" />
                    </a>
                    <c:if test="${authUser.createPlaylists}">
                        <c:choose>
                            <c:when test="${empty sessionScope.playlist}">
                                <a href="${servletUrl}/showPlaylistManager/${auth}" style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                    <fmt:message key="managePlaylists" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${servletUrl}/editPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"
                                   style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                    <fmt:message key="finishPlaylist" />
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${uploadLink}">
                        <a href="${servletUrl}/showUpload/${auth}" style="background-image:url('${appUrl}/images/upload_small.gif');">
                            <fmt:message key="showUpload" />
                        </a>
                    </c:if>
                </td>
            </tr>
        </table>

    </form>

    <jsp:include page="incl_playlist.jsp" />

    <table cellspacing="0">
        <c:if test="${!empty statistics}">
            <tr>
                <th class="active" colspan="3" align="right">
                    <fmt:message key="statistics" />
                </th>
            </tr>
            <tr id="statistics">
                <td colspan="3">
                    <table class="statistics">
                        <tr>
                            <td><fmt:message key="statistics.tracks" />: ${statistics.trackCount}</td>
                            <td><a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>"><fmt:message key="statistics.albums" />: ${statistics.albumCount}</a></td>
                            <td><a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>"><fmt:message key="statistics.artists" />: ${statistics.artistCount}</a></td>
                            <td style="width:100%"><a href="${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>"><fmt:message key="statistics.genres" />: ${statistics.genreCount}</a></td>
                        </tr>
                    </table>
                </td>
            </tr>
        </c:if>
        <tr>
            <th class="active">
                <fmt:message key="playlists" />
            </th>
            <c:if test="${!empty playlists}">
                <th class="active" colspan="2">
                    <fmt:message key="tracks" />
                </th>
            </c:if>
        </tr>

        <c:if test="${!empty container}">
            <tr class="even">
                <td class="homefolder" colspan="3" style="cursor:pointer" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}'">
                    [ / ]
                </td>
            </tr>
            <tr class="odd">
                <td class="parentfolder" colspan="3" style="cursor:pointer" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">cid=${container.containerId}</mt:encrypt>'">
                    [ .. ]
                </td>
            </tr>
        </c:if>

        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td class="${fn:toLowerCase(playlist.type)}" <c:if test="${playlist.type == 'ITunesFolder'}">style="cursor:pointer" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">cid=${playlist.id}</mt:encrypt>'"</c:if>>
                    <c:out value="${playlist.name}" />
                </td>
                <td class="tracks">
                    <c:choose>
                        <c:when test="${playlist.trackCount >= 0}">
                            <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${playlist.trackCount} </a>
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="icon">
                    <c:if test="${authUser.remoteControl && config.remoteControl && globalConfig.remoteControl}">
                        <a href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                            <img src="${appUrl}/images/remote_control${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                 alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.rss && config.showRss}">
                        <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.xml">
                            <img src="${appUrl}/images/rss${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.playlist && config.showPlaylist}">
                        <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.${config.playlistFileSuffix}">
                            <img src="${appUrl}/images/playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.player && config.showPlayer}">
                        <a style="cursor:pointer" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=playlist=${playlist.id}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:webSafeFileName(playlist.name)}.xspf</mt:encrypt>'); return false">
                            <img src="${appUrl}/images/player${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.download && config.showDownload}">
                        <c:choose>
                            <c:when test="${authUser.maximumZipEntries <= 0 || playlist.trackCount <= authUser.maximumZipEntries}">
                                <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.zip">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                         alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                            </c:when>
                            <c:otherwise>
                                <a style="cursor:pointer" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false">
                                    <img src="${appUrl}/images/download${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty playlists}">
            <tr>
                <td><em>
                    <fmt:message key="noPlaylists" />
                </em></td>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty pager}">
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPortal/${auth}/index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

</body>

</html>
