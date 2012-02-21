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
<%--@elvariable id="msgUnknownArtist" type="java.lang.String"--%>
<%--@elvariable id="msgUnknownAlbum" type="java.lang.String"--%>
<%--@elvariable id="msgUnknownTrack" type="java.lang.String"--%>
<%--@elvariable id="tracks" type="java.util.Collection<de.codewave.mytunesrss.TrackUtils.EnhancedTrack>"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=${sortOrder}/playlistName=${param.playlistName}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="browse">

<div class="body">

<div class="head">
    <h1 class="<c:choose><c:when test="${!empty param.searchTerm}">searchResult</c:when><c:otherwise>browse</c:otherwise></c:choose>">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
        <span><fmt:message key="myTunesRss"/></span>
    </h1>
</div>

<div class="content">

<div class="content-inner">

<ul class="menu">
    <c:if test="${sortOrderLink}">
        <li class="first">
            <c:if test="${sortOrder != 'Album'}"><a id="linkByAlbum" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=Album</mt:encrypt>/backUrl=${param.backUrl}"><fmt:message key="groupByAlbum" /></a></c:if>
            <c:if test="${sortOrder != 'Artist'}"><a id="linkByArtist" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=Artist</mt:encrypt>/backUrl=${param.backUrl}"><fmt:message key="groupByArtist" /></a></c:if>
        </li>
    </c:if>
    <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
        <li <c:if test="${!sortOrderLink}">class="first"</c:if>>
            <c:choose>
                <c:when test="${empty editablePlaylists || simpleNewPlaylist}">
                    <a id="linkNewPlaylist" href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="newPlaylist"/></a>
                </c:when>
                <c:otherwise>
                    <a id="linkEditPlaylist" style="cursor:pointer" onclick="openDialog('#editPlaylistDialog')"><fmt:message key="editExistingPlaylist"/></a>
                </c:otherwise>
            </c:choose>
        </li>
    </c:if>
    <li class="spacer">&nbsp;</li>
    <li class="back">
        <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
    </li>
</ul>

<jsp:include page="/incl_error.jsp" />

<jsp:include page="incl_playlist.jsp" />

<table cellspacing="0" class="tracklist searchResult">
    <!-- begin playlist header -->
    <c:set var="fnCount" value="0" />
    <c:if test="${!empty param.playlistName}">
        <tr>
            <th id="functionsDialogName${fnCount}" class="active" colspan="2">
                <c:out value="${mtfn:decode64(param.playlistName)}"/>
            </th>
            <th class="actions">
                <c:set var="filename" value="${mtfn:decode64(param.playlistName)}"/>
                <c:set var="linkFragment" value="playlist=${cwfn:encodeUrl(param.playlist)}"/>
                <c:choose>
                    <c:when test="${!stateEditPlaylist}">
                        <mttag:actions index="${fnCount}"
                                       backUrl="${mtfn:encode64(backUrl)}"
                                       linkFragment="${linkFragment}"
                                       filename="${mtfn:webSafeFileName(filename)}"
                                       zipFileCount="${fn:length(tracks)}"
                                       editTagsType="Playlist"
                                       editTagsId="${param.playlist}"
                                       defaultPlaylistName="${mtfn:webSafeFileName(filename)}"
                                       shareText="${filename}"/>

                    </c:when>
                    <c:otherwise>
                        <c:if test="${authUser.player && config.showPlayer}">
                            <a id="linkEditPlaylistFlashPlaylistHeader" class="flash"
                               onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=<mt:encode64>${linkFragment}/filename=${mtfn:webSafeFileName(filename)}.xspf</mt:encode64></mt:encrypt>/playerId='); return false;"
                               title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                        </c:if>
                        <a id="linkAddToPlaylistPlaylistHeader" class="add"
                           onclick="addPlaylistTracksToPlaylist('${param.playlist}')"
                           alt="add"><span>Add</span></a>
                    </c:otherwise>
                </c:choose>
            </th>
        </tr>
        <c:set var="fnCount" value="${fnCount + 1}" />
    </c:if>
    <!-- end playlist header -->
    <c:forEach items="${tracks}" var="track" varStatus="loopStatus">
        <c:if test="${track.newSection}">
            <c:set var="sectionFileName" value=""/>
            <tr>
                <th id="functionsDialogName${fnCount}" class="active" colspan="2">

                    <c:if test="${config.showThumbnailsForAlbums && !empty(track.imageHash) && sortOrder == 'Album'}">
                        <div class="albumCover">
                            <img id="albumthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                            <div class="tooltip" id="tooltip_albumthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
                        </div>
                    </c:if>

                    <c:choose>
                        <c:when test="${sortOrder == 'Album'}">
                            <a id="linkBrowseAlbumsSection${loopStatus.index}" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${mtfn:encode64(track.albumArtist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.albumArtist), msgUnknownArtist, track.albumArtist)}" />
                            </a>
                            -
                            <a id="linkBrowseTrackSection${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${mtfn:encode64(track.album)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknownAlbum, track.album)}" />
                            </a>
                            <c:set var="sectionFileName">${cwfn:choose(mtfn:unknown(track.artist), msgUnknownArtist, track.artist)} - ${cwfn:choose(mtfn:unknown(track.album), msgUnknownAlbum, track.album)}</c:set>
                        </c:when>
                        <c:otherwise>
                            <a id="linkBrowseAlbumSection${loopStatus.index}" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${mtfn:encode64(track.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknownArtist, track.artist)}" />
                            </a>
                            <c:set var="sectionFileName" value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknownArtist, track.artist)}" />
                            <c:if test="${track.simple}">
                                <c:set var="sectionFileName">${sectionFileName} - ${cwfn:choose(mtfn:unknown(track.album), msgUnknownAlbum, track.album)}</c:set>
                                -
                                <a id="linkBrowseTrackSection${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${mtfn:encode64(track.album)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknownAlbum, track.album)}" />
                                </a>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </th>
                <c:set var="sectionArguments"><c:choose><c:when test="${empty track.sectionPlaylistId}">tracklist=${cwfn:encodeUrl(track.sectionIds)}</c:when><c:otherwise>playlist=${cwfn:encodeUrl(track.sectionPlaylistId)}</c:otherwise></c:choose></c:set>
                <th class="actions">
                    <c:choose>
                        <c:when test="${!stateEditPlaylist}">
                            <mttag:actions index="${fnCount}"
                                           backUrl="${mtfn:encode64(backUrl)}"
                                           linkFragment="${sectionArguments}"
                                           filename="${mtfn:webSafeFileName(sectionFileName)}"
                                           zipFileCount="${mtfn:sectionTrackCount(track.sectionIds)}"
                                           editTagsType="${cwfn:choose(empty track.sectionPlaylistId, 'Track', 'Playlist')}"
                                           editTagsId="${cwfn:choose(empty track.sectionPlaylistId, cwfn:encodeUrl(track.sectionIds), track.sectionPlaylistId)}"
                                           defaultPlaylistName="${sectionFileName}"
                                           shareText="${sectionFileName}" />

                        </c:when>
                        <c:otherwise>
                            <c:if test="${authUser.player && config.showPlayer}">
                                <a id="linkEditPlaylistFlashSection${loopStatus.index}" class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=<mt:encode64>${sectionArguments}/filename=${mtfn:webSafeFileName(sectionFileName)}.xspf</mt:encode64></mt:encrypt>/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                            </c:if>
                            <a id="linkAddToPlaylistSection${loopStatus.index}" class="add" onclick="addTracksToPlaylist(jQuery.makeArray([${mtfn:jsArray(fn:split(track.sectionIds, ","))}]))" alt="add"><span>Add</span></a>
                        </c:otherwise>
                    </c:choose>
                </th>
            </tr>
            <c:set var="fnCount" value="${fnCount + 1}" />
        </c:if>
        <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
            <c:set var="showArtistColumn" value="${((sortOrder == 'Album' && !track.simple) || !empty param['playlist']) && !mtfn:unknown(track.artist)}" />
            <c:set var="showAlbumColumn" value="${sortOrder == 'Artist' && !track.simple && !mtfn:unknown(track.album)}" />
            <td class="artist<c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}"> coverThumbnailColumn</c:if>" <c:if test="${!showAlbumColumn && !showArtistColumn}">colspan="2"</c:if>>
                <div class="trackName">
                    <c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}">
                        <div class="albumCover">
                            <img id="trackthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                            <div class="tooltip" id="tooltip_trackthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
                        </div>
                    </c:if>
                    <c:if test="${track.protected}"><img src="${appUrl}/images/protected${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle"/></c:if>
                    <a id="functionsDialogName${fnCount}"
                       href="${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                       onmouseover="showTooltip(this)"
                       onmouseout="hideTooltip(this)"
                       <c:if test="${track.mediaType == 'Video'}">title="<fmt:message key="video"/>" class="${cwfn:choose(track.videoType == 'Movie', 'movie', 'tvshow')}"</c:if>>
                        <c:choose>
                            <c:when test="${!empty param['playlist']}">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknownTrack, track.name)}" />
                            </c:when>
                            <c:when test="${sortOrder == 'Album'}">
                                <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                                <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknownTrack, track.name)}" />
                            </c:when>
                            <c:otherwise>
                                <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknownTrack, track.name)}" />
                            </c:otherwise>
                        </c:choose>
                        <c:if test="${!empty track.comment}">
                            <div class="tooltip" id="tooltip_functionsDialogName${fnCount}">
                                <c:forEach var="comment" varStatus="loopStatus" items="${mtfn:splitComments(track.comment)}">
                                    <c:out value="${comment}"/>
                                    <c:if test="${!loopStatus.last}"><br /></c:if>
                                </c:forEach>
                            </div>
                        </c:if>
                    </a>
                </div>
            </td>
            <c:choose>
                <c:when test="${showArtistColumn}">
                    <td>
                        <a id="linkBrowseAlbum${fnCount}" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${mtfn:encode64(track.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                            <c:out value="${track.artist}" />
                        </a>
                    </td>
                </c:when>
                <c:when test="${showAlbumColumn}">
                    <td>
                        <a id="linkBrowseAlbum${fnCount}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${mtfn:encode64(track.album)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                            <c:out value="${track.album}" />
                        </a>
                    </td>
                </c:when>
            </c:choose>
            <td class="actions">
                <c:choose>
                    <c:when test="${!stateEditPlaylist}">
                        <c:set var="shareText"><c:choose><c:when test="${mtfn:unknown(track.artist)}">${track.name}</c:when><c:otherwise>${track.artist} - ${track.name}</c:otherwise></c:choose></c:set>
                        <mttag:actions index="${fnCount}"
                                       backUrl="${mtfn:encode64(backUrl)}"
                                       linkFragment="track=${track.id}"
                                       filename="${mtfn:virtualTrackName(track)}"
                                       track="${track}"
                                       externalSitesFlag="${mtfn:externalSites('title') && authUser.externalSites}"
                                       editTagsType="Track"
                                       editTagsId="${track.id}"
                                       defaultPlaylistName="${track.name}"
                                       shareText="${shareText}" />
                    </c:when>
                    <c:otherwise>
                        <c:if test="${authUser.player && config.showPlayer}">
                            <a id="linkEditPlaylistFlash${fnCount}" class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=<mt:encode64>track=${track.id}/filename=${mtfn:virtualTrackName(track)}.xspf</mt:encode64></mt:encrypt>/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                        </c:if>
                        <c:if test="${mtfn:lowerSuffix(pageContext, config, authUser, track) eq 'mp3' && authUser.yahooPlayer && config.yahooMediaPlayer}">
                            <c:set var="yahoo" value="true"/>
                            <a id="linkEditPlaylistYahoo${fnCount}" class="htrack" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>">
                                <img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=64</mt:encrypt>" style="display:none" alt=""/>
                            </a>
                        </c:if>
                        <a id="linkAddToPlaylist${fnCount}" class="add" onclick="addTracksToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(track.id)}']))" title="<fmt:message key="playlist.addToPlaylist"/>"><span><fmt:message key="playlist.addToPlaylist"/></span></a>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
        <c:set var="fnCount" value="${fnCount + 1}"/>
    </c:forEach>
</table>

<c:if test="${!empty pager}">
    <c:set var="pagerCommand"
           scope="request">${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/sortOrder=${sortOrder}/playlistName=${param.playlistName}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
    <jsp:include page="incl_bottomPager.jsp" />
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
<jsp:include page="incl_edit_tags.jsp" />

<c:if test="${yahoo}">
    <script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script>
</c:if>

</body>

</html>