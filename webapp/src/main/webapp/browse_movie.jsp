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
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="editablePlaylists" type="java.util.List"--%>
<%--@elvariable id="tracks" type="java.util.List<de.codewave.mytunesrss.datastore.statement.Track>"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseMovie/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="browse">

    <div class="body">
    
        <div class="head">    
            <h1 class="browse">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
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
                                    <a href="${servletUrl}/startNewPlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="newPlaylist"/></a>
                                </c:when>
                                <c:otherwise>
                                    <a style="cursor:pointer" onclick="openDialog('#editPlaylistDialog')"><fmt:message key="editExistingPlaylist"/></a>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:if>
                    <li class="spacer">&nbsp;</li>
                    <li class="back">
                        <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                    </li>
                </ul>
                
                <jsp:include page="/incl_error.jsp" />
                
                <jsp:include page="incl_playlist.jsp" />
                
                <table cellspacing="0" class="tracklist searchResult">
                <c:set var="fnCount" value="0" />
                <c:forEach items="${tracks}" var="track" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td class="artist<c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}"> coverThumbnailColumn</c:if>">
                        <div class="trackName">
                            <c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}">
                            	<div class="albumCover">
                                <img class="coverThumbnail" id="trackthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                                <div class="tooltip" id="tooltip_trackthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
	                            </div>
                            </c:if>
                            <c:if test="${track.protected}"><img src="${appUrl}/images/protected${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle"/></c:if>
                            <a id="functionsDialogName${fnCount}"
                               href="${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                               onmouseover="showTooltip(this)"
                               onmouseout="hideTooltip(this)"
							   title="<fmt:message key="video"/>" class="movie">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknownTrack, track.name)}" />
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
                    <td class="actions">
                        <c:choose>
                            <c:when test="${!stateEditPlaylist}">
                                <mttag:actions index="${fnCount}"
                                               backUrl="${mtfn:encode64(backUrl)}"
                                               linkFragment="track=${track.id}"
                                               filename="${mtfn:virtualTrackName(track)}"
                                               track="${track}"
                                               externalSitesFlag="${mtfn:externalSites('title') && authUser.externalSites}"
                                               editTagsType="Track"
                                               editTagsId="${track.id}"
                                               defaultPlaylistName="${track.name}"
                                               shareText="${track.name}" />
                            </c:when>
                            <c:otherwise>
                                <c:if test="${authUser.player && config.showPlayer}">
                                    <a class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/playerId=#ID#/<mt:encrypt key="${encryptionKey}">playlistParams=track=${track.id}/filename=${mtfn:virtualTrackName(track)}.xspf</mt:encrypt>'); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                                </c:if>
                                <a class="add" onclick="addTracksToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(track.id)}']))" title="<fmt:message key="playlist.addToPlaylist"/>"><span><fmt:message key="playlist.addToPlaylist"/></span></a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <c:set var="fnCount" value="${fnCount + 1}"/>
                </c:forEach>
                </table>
                
                <c:if test="${!empty pager}">
                    <c:set var="pagerCommand"
                           scope="request">${servletUrl}/browseMovie/${auth}/index={index}/backUrl=${param.backUrl}</c:set>
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

</body>

</html>