<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="mttag" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="editablePlaylists" type="java.util.List"--%>
<%--@elvariable id="tracks" type="java.util.List<de.codewave.mytunesrss.TrackUtils.TvShowEpisode>"--%>
<%--@elvariable id="msgUnknownSeries" type="java.lang.String"--%>
<%--@elvariable id="msgUnknownTrack" type="java.lang.String"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseTvShow/${auth}/<mt:encrypt key="${encryptionKey}">series=${cwfn:encodeUrl(param.series)}/season=${cwfn:encodeUrl(param.season)}/index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="browse">

<jsp:include page="incl_edit_tags.jsp"/>

<div class="body">

    <div class="head">
        <h1 class="browse">
            <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
            <span><fmt:message key="myTunesRss"/></span>
        </h1>
    </div>

    <div class="content">

        <div class="content-inner">

            <ul class="menu">
                <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
                    <li class="first">
                        <c:choose>
                            <c:when test="${empty editablePlaylists || simpleNewPlaylist}">
                                <a id="linkNewPlaylist" href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message
                                        key="newPlaylist"/></a>
                            </c:when>
                            <c:otherwise>
                                <a id="linkEditPlaylist" style="cursor:pointer"
                                   onclick="openDialog('#editPlaylistDialog')"><fmt:message
                                        key="editExistingPlaylist"/></a>
                            </c:otherwise>
                        </c:choose>
                    </li>
                </c:if>
                <li class="spacer">&nbsp;</li>
                <li class="back">
                    <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                </li>
            </ul>

            <jsp:include page="/incl_error.jsp"/>

            <jsp:include page="incl_playlist.jsp"/>

            <table cellspacing="0" class="tracklist searchResult">
                <c:set var="fnCount" value="0"/>
                <c:if test="${!empty tracks[0].id || tracks[0].season != -1}">
                    <tr>
                        <th class="active" colspan="2">
                            <c:choose>
                                <c:when test="${empty tracks[0].id}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(tracks[0].series), msgUnknownSeries, tracks[0].series)}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${cwfn:choose(mtfn:unknown(tracks[0].series), msgUnknownSeries, tracks[0].series)}"/> - <fmt:message key="season"/> ${tracks[0].season}
                                </c:otherwise>
                            </c:choose>
                        </th>
                    </tr>
                </c:if>
                <c:forEach items="${tracks}" var="track" varStatus="loopStatus">
                    <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                        <td class="artist<c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}"> coverThumbnailColumn</c:if>">
                            <div class="trackName">
                                <c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}">
                                    <div class="albumCover">
                                        <img id="trackthumb_${loopStatus.index}"
                                             src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=32</mt:encrypt>"
                                             onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>

                                        <div class="tooltip" id="tooltip_trackthumb_${loopStatus.index}"><img
                                                src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=${config.albumImageSize}</mt:encrypt>"
                                                alt=""/></div>
                                    </div>
                                </c:if>
                                <c:if test="${track.protected}"><img
                                        src="${appUrl}/images/protected${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif"
                                        alt="<fmt:message key="protected"/>" style="vertical-align:middle"/></c:if>
                                <a id="functionsDialogName${fnCount}"
                                   <c:choose>
                                       <c:when test="${empty track.id && track.season == -1}">
                                           href="${servletUrl}/browseTvShow/${auth}/<mt:encrypt
                                               key="${encryptionKey}">series=${mtfn:encode64(track.series)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                                       </c:when>
                                       <c:when test="${empty track.id}">
                                           href="${servletUrl}/browseTvShow/${auth}/<mt:encrypt
                                               key="${encryptionKey}">series=${mtfn:encode64(track.series)}/season=${track.season}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                                       </c:when>
                                       <c:otherwise>
                                           href="${servletUrl}/showTrackInfo/${auth}/<mt:encrypt
                                               key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                                       </c:otherwise>
                                   </c:choose>
                                   onmouseover="showTooltip(this)"
                                   onmouseout="hideTooltip(this)"
                                   title="<fmt:message key="video"/>" class="tvshow">
                                    <c:choose>
                                        <c:when test="${empty track.id && track.season == -1}">
                                            <c:out value="${cwfn:choose(mtfn:unknown(track.series), msgUnknownSeries, track.series)}"/>
                                        </c:when>
                                        <c:when test="${empty track.id}">
                                            <fmt:message key="season"/> ${track.season}
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknownTrack, track.name)}"/>
                                        </c:otherwise>
                                    </c:choose>
                                    <c:if test="${!empty track.id && !empty track.comment}">
                                        <div class="tooltip" id="tooltip_functionsDialogName${fnCount}">
                                            <c:forEach var="comment" varStatus="loopStatus"
                                                       items="${mtfn:splitComments(track.comment)}">
                                                <c:out value="${comment}"/>
                                                <c:if test="${!loopStatus.last}"><br/></c:if>
                                            </c:forEach>
                                        </div>
                                    </c:if>
                                </a>
                            </div>
                        </td>
                        <td class="actions">
                            <c:choose>
                                <c:when test="${empty track.id && track.season == -1}">
                                    <c:set var="linkFragment"><c:choose><c:when test="${empty track.seriesSectionPlaylistId}">tracklist=${cwfn:encodeUrl(track.seriesSectionIds)}</c:when><c:otherwise>playlist=${cwfn:encodeUrl(track.seriesSectionPlaylistId)}</c:otherwise></c:choose></c:set>
                                    <c:set var="filename">${mtfn:webSafeFileName(track.series)}</c:set>
                                    <c:set var="playlistName">${track.series}</c:set>
                                    <c:set var="editTagsResource">PlaylistResource</c:set>
                                    <c:set var="editTagsParams">{track:'${track.seriesSectionPlaylistId}'}</c:set>
                                </c:when>
                                <c:when test="${empty track.id}">
                                    <c:set var="linkFragment"><c:choose><c:when test="${empty track.seasonSectionPlaylistId}">tracklist=${cwfn:encodeUrl(track.seasonSectionIds)}</c:when><c:otherwise>playlist=${cwfn:encodeUrl(track.seasonSectionPlaylistId)}</c:otherwise></c:choose></c:set>
                                    <c:set var="filename">${track.series} - <fmt:message key="season"/> ${track.season}</c:set>
                                    <c:set var="filename">${mtfn:webSafeFileName(filename)}</c:set>
                                    <c:set var="playlistName">${track.series} - <fmt:message key="season"/> ${track.season}</c:set>
                                    <c:set var="editTagsResource">PlaylistResource</c:set>
                                    <c:set var="editTagsParams">{playlist:'${track.seasonSectionPlaylistId}'}</c:set>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="linkFragment">track=${track.id}</c:set>
                                    <c:set var="filename">${mtfn:webSafeFileName(track.name)}</c:set>
                                    <c:set var="playlistName">${track.name}</c:set>
                                    <c:set var="editTagsResource">TrackResource</c:set>
                                    <c:set var="editTagsParams">{track:'${track.id}'}</c:set>
                                </c:otherwise>
                            </c:choose>
                            <c:choose>
                                <c:when test="${!stateEditPlaylist}">
                                    <mttag:actions index="${fnCount}"
                                                   backUrl="${mtfn:encode64(backUrl)}"
                                                   linkFragment="${linkFragment}"
                                                   filename="${filename}"
                                                   track="${cwfn:choose(!empty track.id , track, null)}"
                                                   externalSitesFlag="${mtfn:externalSites('title') && authUser.externalSites}"
                                                   editTagsResource="${editTagsResource}"
                                                   editTagsParams="${editTagsParams}"
                                                   defaultPlaylistName="${playlistName}"
                                                   shareText="${playlistName}"/>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${globalConfig.flashPlayer && authUser.player && config.showPlayer}">
                                        <a id="linkEditPlaylistFlash${loopStatus.index}" class="flash"
                                           onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=<mt:encode64>${linkFragment}/filename=${filename}.xspf</mt:encode64></mt:encrypt>/playerId='); return false;"
                                           title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                                    </c:if>
                                    <a id="linkAddToPlaylist${loopStatus.index}" class="add"
                                       onclick="addTracksToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(track.id)}']))"
                                       title="<fmt:message key="playlist.addToPlaylist"/>"><span><fmt:message
                                            key="playlist.addToPlaylist"/></span></a>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                    <c:set var="fnCount" value="${fnCount + 1}"/>
                </c:forEach>
            </table>

            <c:if test="${!empty pager}">
                <c:set var="pagerCommand"
                       scope="request">${servletUrl}/browseTvShow/${auth}/<mt:encrypt
                        key="${encryptionKey}">series=${cwfn:encodeUrl(param.series)}/season=${cwfn:encodeUrl(param.season)}/backUrl=${param.backUrl}</mt:encrypt>/index={index}</c:set>
                <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}"/>
                <jsp:include page="incl_bottomPager.jsp"/>
            </c:if>

        </div>

    </div>

    <div class="footer">
        <div class="inner"></div>
    </div>

</div>

<jsp:include page="incl_select_flashplayer_dialog.jsp"/>
<jsp:include page="incl_edit_playlist_dialog.jsp"/>

<c:set var="externalSiteDefinitions" scope="request" value="${mtfn:externalSiteDefinitions('title')}"/>
<jsp:include page="incl_external_sites_dialog.jsp"/>
<jsp:include page="incl_functions_menu.jsp"/>

</body>

</html>