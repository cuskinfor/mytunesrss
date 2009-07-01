<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl" scope="request">${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=${sortOrder}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        $jQ(document).ready(function() {
            $jQ(".externalSite").dialog({
                autoOpen:false,
                modal:true,
                buttons:{
                    '<fmt:message key="dialog.button.close"/>':function() {
                        $jQ(this).dialog("close");
                    }
                }
            })
        });
    </script>

</head>

<body>

<div class="body">

<h1 class="<c:choose><c:when test="${!empty param.searchTerm}">searchResult</c:when><c:otherwise>browse</c:otherwise></c:choose>">
    <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
</h1>

<jsp:include page="/incl_error.jsp" />

<ul class="links">
    <c:if test="${sortOrderLink}">
        <li>
            <c:if test="${sortOrder != 'Album'}"><a style="cursor:pointer" onclick="sort('${servletUrl}', '${auth}', 'Album'); return false"><fmt:message key="groupByAlbum" /></a></c:if>
            <c:if test="${sortOrder != 'Artist'}"><a style="cursor:pointer" onclick="sort('${servletUrl}', '${auth}', 'Artist'); return false"><fmt:message key="groupByArtist" /></a></c:if>
        </li>
    </c:if>
    <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
        <li>
            <c:choose>
                <c:when test="${empty editablePlaylists || simpleNewPlaylist}">
                    <a href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="newPlaylist"/></a>
                </c:when>
                <c:otherwise>
                    <a style="cursor:pointer" onclick="$jQ('#editPlaylistDialog').dialog('open')"><fmt:message key="editExistingPlaylist"/></a>
                </c:otherwise>
            </c:choose>
        </li>
    </c:if>
    <li style="float:right;">
        <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
    </li>
</ul>

<jsp:include page="incl_playlist.jsp" />

<table cellspacing="0">
<c:forEach items="${tracks}" var="track" varStatus="loopStatus">
<c:if test="${track.newSection}">
    <c:set var="sectionFileName" value=""/>
    <c:set var="count" value="0" />
    <tr>
        <th class="active" colspan="2">
            <c:choose>
                <c:when test="${sortOrder == 'Album'}">
                    <c:if test="${track.simple}">
                        <c:set var="sectionFileName">${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)} -</c:set>
                        <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(track.artist))}</mt:encrypt>">
                            <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
                        </a> -</c:if>
                    <c:set var="sectionFileName">${sectionFileName} ${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}</c:set>
                    <c:choose>
                        <c:when test="${empty param.album}">
                            <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(track.album))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" />
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(track.artist))}</mt:encrypt>">
                        <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
                    </a>
                    <c:set var="sectionFileName" value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
                    <c:if test="${track.simple}">
                        <c:set var="sectionFileName">${sectionFileName} - ${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}</c:set>
                        -
                        <c:choose>
                            <c:when test="${empty param.album}">
                                <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(track.album))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" />
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </th>
        <c:set var="sectionArguments"><c:choose><c:when test="${empty track.sectionPlaylistId}">tracklist=${cwfn:encodeUrl(track.sectionIds)}</c:when><c:otherwise>playlist=${cwfn:encodeUrl(track.sectionPlaylistId)}</c:otherwise></c:choose></c:set>
        <th class="icon">
            <c:choose>
                <c:when test="${!stateEditPlaylist}">
                    <c:if test="${authUser.remoteControl && config.remoteControl && globalConfig.remoteControl}">
                        <c:choose>
                            <c:when test="${empty track.sectionPlaylistId}">
                                <a href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">tracklist=${cwfn:encodeUrl(track.sectionIds)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <img src="${appUrl}/images/remote_control_th.gif"
                                         alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${track.sectionPlaylistId}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <img src="${appUrl}/images/remote_control_th.gif"
                                         alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${authUser.rss && config.showRss}">
                        <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">${sectionArguments}</mt:encrypt>/${mtfn:webSafeFileName(sectionFileName)}.xml">
                            <img src="${appUrl}/images/rss_th.gif" alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.playlist && config.showPlaylist}">
                        <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${sectionArguments}</mt:encrypt>/${mtfn:webSafeFileName(sectionFileName)}.${config.playlistFileSuffix}">
                            <img src="${appUrl}/images/playlist_th.gif" alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.player && config.showPlayer}">
                        <a style="cursor:pointer" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=${sectionArguments}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:webSafeFileName(sectionFileName)}.xspf</mt:encrypt>'); return false">
                            <img src="${appUrl}/images/player_th.gif" alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                    </c:if>
                    <c:if test="${authUser.download && config.showDownload}">
                        <c:choose>
                            <c:when test="${authUser.maximumZipEntries <= 0 || mtfn:sectionTrackCount(track.sectionIds) <= authUser.maximumZipEntries}">
                                <a href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">${sectionArguments}</mt:encrypt>/${mtfn:webSafeFileName(sectionFileName)}.zip">
                                <img src="${appUrl}/images/download_th.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                            </c:when>
                            <c:otherwise>
                                <a style="cursor:pointer" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false">
                                <img src="${appUrl}/images/download_th.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <a style="cursor:pointer" onclick="addTracksToPlaylist($A(${mtfn:jsArray(fn:split(track.sectionIds, ","))}))"><img src="${appUrl}/images/add_th.gif" alt="add" /></a>
                </c:otherwise>
            </c:choose>
        </th>
    </tr>
</c:if>
<tr class="${cwfn:choose(count % 2 == 0, 'even', 'odd')}">
    <td class="artist" <c:if test="${!(sortOrder == 'Album' && !track.simple)}">colspan="2"</c:if>>
        <c:if test="${config.showThumbnails && track.imageCount > 0}">
            <img id="trackthumb_${loopStatus.index}" src="${servletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
            <div class="tooltip" id="tooltip_trackthumb_${loopStatus.index}"><img src="${servletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
        </c:if>
        <c:if test="${track.protected}"><img src="${appUrl}/images/protected${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle"/></c:if>
        <c:choose>
            <c:when test="${track.source.jspName == 'YouTube'}"><img src="${appUrl}/images/youtube${cwfn:choose(count % 2 == 0, '', '_odd')}.png" alt="<fmt:message key="video"/>" style="vertical-align:middle"/></c:when>
            <c:when test="${track.mediaType.jspName == 'Video'}"><img src="${appUrl}/images/movie${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="video"/>" style="vertical-align:middle"/></c:when>
        </c:choose>
        <a id="tracklink_${track.id}" href="${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)">
            <c:choose>
                <c:when test="${!empty param['playlist']}">
                    <c:if test="${!mtfn:unknown(track.artist)}"><c:out value="${track.artist}"/> -</c:if>
                    <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknown, track.name)}" />
                </c:when>
                <c:when test="${sortOrder == 'Album'}">
                    <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                    <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknown, track.name)}" />
                </c:when>
                <c:otherwise>
                    <c:if test="${!track.simple && !mtfn:unknown(track.album)}"><c:out value="${track.album}" /> -</c:if>
                    <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                    <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknown, track.name)}" />
                </c:otherwise>
            </c:choose>
            <c:if test="${!empty track.comment}">
                <div class="tooltip" id="tooltip_tracklink_${track.id}">
                    <c:forEach var="comment" varStatus="loopStatus" items="${mtfn:splitComments(track.comment)}">
                        <c:out value="${comment}"/>
                        <c:if test="${!loopStatus.last}"><br /></c:if>
                    </c:forEach>
                </div>
            </c:if>
        </a>
    </td>
    <c:if test="${sortOrder == 'Album' && !track.simple}">
        <td>
            <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(track.artist))}</mt:encrypt>">
                <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
            </a>
        </td>
    </c:if>
    <td class="icon">
        <c:choose>
            <c:when test="${!stateEditPlaylist}">
                <c:if test="${mtfn:externalSites('title')}">
                    <img src="${appUrl}/images/http.gif" alt="external site" title="external site" style="cursor:pointer" onclick="$jQ('#extSite${loopStatus.index}').dialog('open')"/>
                    <div id="extSite${loopStatus.index}" class="externalSite" style="display:none" title="<fmt:message key="dialog.externalSite.title"/>">
                        <c:forEach items="${mtfn:externalSiteDefinitions('title', track.name)}" var="externalSite" varStatus="siteLoopStatus">
                            <a href="${externalSite.value}" target="_blank" onclick="$jQ(this).closest('div').dialog('close')"><c:out value="${externalSite.key}"/></a>
                            <c:if test="${!siteLoopStatus.last}"><br /></c:if>
                        </c:forEach>
                    </div>
                </c:if>
                <c:if test="${authUser.remoteControl && config.remoteControl && globalConfig.remoteControl}">
                    <a href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                        <img src="${appUrl}/images/remote_control${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                             alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
                </c:if>
                <c:if test="${authUser.rss && config.showRss}">
                    <a href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.xml">
                        <img src="${appUrl}/images/rss${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                </c:if>
                <c:if test="${authUser.playlist && config.showPlaylist}">
                    <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.${config.playlistFileSuffix}">
                        <img src="${appUrl}/images/playlist${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                </c:if>
                <c:if test="${authUser.player && config.showPlayer}">
                    <a style="cursor:pointer" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=track=${track.id}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:virtualTrackName(track)}.xspf</mt:encrypt>'); return false">
                        <img src="${appUrl}/images/player${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                </c:if>
                <c:if test="${authUser.download && config.showDownload}">
                        <c:choose>
                            <c:when test="${!config.yahooMediaPlayer || mtfn:lowerSuffix(config, authUser, track) ne 'mp3'}">
                                <a href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>"/>
                                    <img src="${appUrl}/images/download${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playtrack"/>" title="<fmt:message key="tooltip.playtrack"/>" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <c:set var="yahoo" value="true"/>
                                <a class="htrack" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>" title="<c:out value="${track.name}"/>">
                                    <img src="${servletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/size=64</mt:encrypt>" style="display:none" alt=""/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </a>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:if test="${mtfn:lowerSuffix(config, authUser, track) eq 'mp3' && config.showDownload && authUser.download && config.yahooMediaPlayer}">
                    <c:set var="yahoo" value="true"/>
                    <a class="htrack" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>"/>
                        <img src="${servletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/size=64</mt:encrypt>" style="display:none" alt=""/>
                    </a>
                </c:if>
                <a style="cursor:pointer" onclick="addTracksToPlaylist($A(['${mtfn:escapeJs(track.id)}']))">
                    <img src="${appUrl}/images/add${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
            </c:otherwise>
        </c:choose>
    </td>
</tr>
<c:set var="count" value="${count + 1}" />
</c:forEach>
</table>

<c:if test="${!empty pager}">
    <c:set var="pagerCommand"
           scope="request">${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/sortOrder=${sortOrder}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
    <jsp:include page="incl_bottomPager.jsp" />
</c:if>

</div>

<jsp:include page="incl_edit_playlist_dialog.jsp"/>

<c:if test="${yahoo}"><script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script></c:if>

</body>

</html>