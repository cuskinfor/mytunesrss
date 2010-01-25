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

<c:set var="backUrl" scope="request">${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=${sortOrder}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="browse">

    <div class="body">
    
        <div class="head">    
            <h1 class="<c:choose><c:when test="${!empty param.searchTerm}">searchResult</c:when><c:otherwise>browse</c:otherwise></c:choose>">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>
        
        <div class="content">
            
            <div class="content-inner">
                
                <ul class="menu">
                    <c:if test="${sortOrderLink}">
                        <li class="first">
                            <c:if test="${sortOrder != 'Album'}"><a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=Album</mt:encrypt>/backUrl=${param.backUrl}"><fmt:message key="groupByAlbum" /></a></c:if>
                            <c:if test="${sortOrder != 'Artist'}"><a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/index=${param.index}/sortOrder=Artist</mt:encrypt>/backUrl=${param.backUrl}"><fmt:message key="groupByArtist" /></a></c:if>
                        </li>
                    </c:if>
                    <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
                        <li <c:if test="${!sortOrderLink}">class="first"</c:if>>
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
                <c:if test="${track.newSection}">
                    <c:set var="sectionFileName" value=""/>
                    <tr>
                        <th id="functionsDialogName${fnCount}" class="active" colspan="2">

						    <c:if test="${config.showThumbnailsForAlbums && !empty(track.imageHash) && sortOrder == 'Album'}">
							    <div class="albumCover">
									<img id="albumthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${cwfn:encodeUrl(track.imageHash)}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
									<div class="tooltip" id="tooltip_albumthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${cwfn:encodeUrl(track.imageHash)}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
								</div>
							</c:if>

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
                        <th class="actions">
                            <c:choose>
                                <c:when test="${!stateEditPlaylist}">
                                    <mttag:actions index="${fnCount}"
                                                   backUrl="${mtfn:encode64(backUrl)}"
                                                   linkFragment="${sectionArguments}"
                                                   filename="${mtfn:webSafeFileName(sectionFileName)}"
                                                   zipFileCount="${mtfn:sectionTrackCount(track.sectionIds)}"
                                                   editTagsType="${cwfn:choose(empty track.sectionPlaylistId, 'Track', 'Playlist')}"
                                                   editTagsId="${cwfn:choose(empty track.sectionPlaylistId, cwfn:encodeUrl(track.sectionIds), track.sectionPlaylistId)}" />                                   
                
                                </c:when>
                                <c:otherwise>
                                    <a class="add" onclick="addTracksToPlaylist($A([${mtfn:jsArray(fn:split(track.sectionIds, ","))}]))" alt="add"><span>Add</span></a>
                                </c:otherwise>
                            </c:choose>
                        </th>
                    </tr>
                    <c:set var="fnCount" value="${fnCount + 1}" />
                </c:if>
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td class="artist<c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}"> coverThumbnailColumn</c:if>" <c:if test="${!(sortOrder == 'Album' && !track.simple)}">colspan="2"</c:if>>
                        <div class="trackName">
                            <c:if test="${config.showThumbnailsForTracks && !empty(track.imageHash)}">
                                <img class="coverThumbnail" id="trackthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                                <div class="tooltip" id="tooltip_trackthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
                            </c:if>
                            <c:if test="${track.protected}"><img src="${appUrl}/images/protected${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle"/></c:if>
                            <a id="functionsDialogName${fnCount}"
                               href="${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                               onmouseover="showTooltip(this)"
                               onmouseout="hideTooltip(this)"
								<c:choose>
								    <c:when test="${track.source.jspName == 'YouTube'}">title="<fmt:message key="video"/>" class="youtube"</c:when>
								    <c:when test="${track.mediaType.jspName == 'Video'}">title="<fmt:message key="video"/>" class="movie"</c:when>
								</c:choose>                               
                            >
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
                    <c:if test="${sortOrder == 'Album' && !track.simple}">
                        <td>
                            <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(track.artist))}</mt:encrypt>">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
                            </a>
                        </td>
                    </c:if>
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
                                               editTagsId="${track.id}" />
                            </c:when>
                            <c:otherwise>
                                <c:if test="${mtfn:lowerSuffix(config, authUser, track) eq 'mp3' && config.showDownload && authUser.download && config.yahooMediaPlayer}">
                                    <c:set var="yahoo" value="true"/>
                                    <a class="htrack" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>"/>
                                        <img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=64</mt:encrypt>" style="display:none" alt=""/>
                                    </a>
                                </c:if>
                                <a class="add" onclick="addTracksToPlaylist($A(['${mtfn:escapeJs(track.id)}']))" alt="add"><span>Add</span></a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <c:set var="fnCount" value="${fnCount + 1}"/>
                </c:forEach>
                </table>
                
                <c:if test="${!empty pager}">
                    <c:set var="pagerCommand"
                           scope="request">${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${cwfn:encodeUrl(param.playlist)}/fullAlbums=${param.fullAlbums}/album=${cwfn:encodeUrl(param.album)}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/searchTerm=${cwfn:encodeUrl(param.searchTerm)}/fuzzy=${cwfn:encodeUrl(param.fuzzy)}/sortOrder=${sortOrder}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
                    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
                    <jsp:include page="incl_bottomPager.jsp" />
                </c:if>
                
            </div>
            
        </div>
        
        <div class="footer">
            <div class="inner"></div>
        </div>
    
    </div>
    
    <jsp:include page="incl_edit_playlist_dialog.jsp"/>
    
    <c:set var="externalSiteDefinitions" scope="request" value="${mtfn:externalSiteDefinitions('title')}"/>
    <jsp:include page="incl_external_sites_dialog.jsp"/>
    <jsp:include page="incl_functions_menu.jsp" />
    <jsp:include page="incl_edit_tags.jsp" />
    
    <c:if test="${yahoo}"><script type="text/javascript" src="http://mediaplayer.yahoo.com/js"></script></c:if>

</body>

</html>