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
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="statistics" type="de.codewave.mytunesrss.datastore.statement.SystemInformation"--%>
<%--@elvariable id="photoAlbums" type="java.lang.Boolean"--%>
<%--@elvariable id="pager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="lastSearchTerm" type="java.lang.String"--%>
<%--@elvariable id="uploadLink" type="java.lang.Boolean"--%>
<%--@elvariable id="stateEditPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="container" type="de.codewave.mytunesrss.datastore.statement.Playlist"--%>

<%--@elvariable id="playlists" type="java.util.List"--%>

<c:set var="backUrl" scope="request">${servletUrl}/showPortal/${auth}/<mt:encrypt>index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <link rel="search" type="application/opensearchdescription+xml" href="${servletUrl}/openSearch?username=${mtfn:encode64(authUser.name)}&auth=${mtfn:encode64(auth)}" title="MyTunesRSS" />

    <c:if test="${authUser.rss}">
        <c:forEach items="${playlists}" var="playlist">
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt>playlist=${playlist.id}/_cdi=${cwfn:encodeUrl(playlist.name)}.xml</mt:encrypt>" rel="alternate" type="application/rss+xml" title="<c:out value="${playlist.name}" />" />
        </c:forEach>
    </c:if>

</head>

<body onload="document.forms[0].elements['searchTerm'].focus()" class="startpage">

<div class="body">

    <div class="head">
        <h1 id="linkCodewaveSite" onclick="window.open('http://www.codewave.de')" style="cursor: pointer"><span><fmt:message key="myTunesRss" /></span></h1>
    </div>

    <div class="content">
        <div class="content-inner">

        <ul class="menu">
            <c:if test="${authUser.editWebSettings}">
                <li class="settings first"><a id="linkSettings" href="${servletUrl}/showSettings/${auth}">
                    <fmt:message key="doSettings" />
                </a></li>
            </c:if>
            <c:if test="${globalConfig.serverBrowserActive}">
                <li class="servers <c:if test="${!authUser.editWebSettings}">first</c:if>"><a id="linkBrowseServers" href="${servletUrl}/browseServers/${auth}">
                    <fmt:message key="browseServers" />
                </a></li>
            </c:if>
            <c:if test="${showRemoteControl}">
                <li class="servers <c:if test="${!authUser.editWebSettings && !globalConfig.serverBrowserActive}">first</c:if>"><a id="linkRemoteControl" href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">
                    <fmt:message key="showRemoteControl" />
                </a></li>
            </c:if>
            <c:if test="${authUser.editWebSettings || globalConfig.serverBrowserActive || showRemoteControl}">
	            <li class="spacer">&nbsp;</li>
            </c:if>
            <c:if test="${empty globalConfig.autoLogin}">
                <li class="logout"><a id="linkLogout" href="${servletUrl}/logout">
                    <fmt:message key="doLogout" />
                </a></li>
            </c:if>
        </ul>

        <jsp:include page="/incl_error.jsp" />

        <form id="search" action="${servletUrl}/searchTracks/${auth}" method="post">

            <table class="navigation" cellspacing="0">
            	<tr>
	                <td class="search">
                    <input class="text" type="text" name="searchTerm" value="<c:out value="${lastSearchTerm}"/>"/>
                    <c:choose>
                        <c:when test="${authUser.searchFuzziness == -1}">
                            <select name="searchFuzziness">
                                <option value="0" <c:if test="${config.searchFuzziness == 0}">selected="selected"</c:if>><fmt:message key="search.fuzziness.0"/></option>
                                <option value="35" <c:if test="${config.searchFuzziness == 35}">selected="selected"</c:if>><fmt:message key="search.fuzziness.30"/></option>
                                <option value="-1" <c:if test="${config.searchFuzziness == -1}">selected="selected"</c:if>><fmt:message key="search.expert"/></option>
                            </select>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="searchFuzziness" value="${authUser.searchFuzziness}" />
                        </c:otherwise>
                    </c:choose>
                    <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
                    <input type="hidden" name="sortOrder" value="Album" />
                    <input id="linkDoSearch" class="button" type="submit" value="<fmt:message key="doSearch"/>"/>
	                </td>

                    <mttag:portalLink test="${!globalConfig.disableBrowser && authUser.audio && (statistics.albumCount > 0 || statistics.artistCount > 0 || statistics.genreCount > 0)}">
                        <a id="linkBrowseArtist" class="music" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt>page=${config.browserStartIndex}</mt:encrypt>">
                            <fmt:message key="browseLibraryAudio" />
                        </a>
                    </mttag:portalLink>
                    <mttag:portalLink test="${!globalConfig.disableBrowser && authUser.video && statistics.movieCount > 0}">
                        <a id="linkBrowseMovie" class="movie"
                           href="${servletUrl}/browseMovie/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">
                            <fmt:message key="browseLibraryMovie"/>
                        </a>
                    </mttag:portalLink>
                    <mttag:portalLink test="${!globalConfig.disableBrowser && authUser.video && statistics.tvShowCount > 0}">
                        <a id="linkBrowseTvShow" class="tvshow" href="${servletUrl}/browseTvShow/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">
                            <fmt:message key="browseLibraryTvShow" />
                        </a>
                    </mttag:portalLink>
                    <mttag:portalLink test="${!globalConfig.disableBrowser && statistics.photoCount > 0 && authUser.photos}">
                        <a id="linkBrowsePhotoAlbum" class="photo"
                           href="${servletUrl}/browsePhotoAlbum/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">
                            <fmt:message key="browseLibraryPhoto"/>
                        </a>
                    </mttag:portalLink>
                    <mttag:portalLink test="${authUser.createPlaylists}">
                        <c:choose>
                            <c:when test="${!stateEditPlaylist}">
                                <a id="linkManagePlaylists" class="playlists" href="${servletUrl}/showPlaylistManager/${auth}">
                                    <fmt:message key="managePlaylists" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a id="linkFinishPlaylist" class="playlists" href="${servletUrl}/showResource/${auth}/<mt:encrypt>resource=EditPlaylist/backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>"
                                   style="background-image:url('${themeUrl}/images/feeds_small.gif');">
                                    <fmt:message key="finishPlaylist" />
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </mttag:portalLink>
                    <mttag:portalLink test="${uploadLink}">
                        <a id="linkUpload" class="upload" href="${servletUrl}/showUpload/${auth}">
                            <fmt:message key="showUpload" />
                        </a>
                    </mttag:portalLink>
                </tr>
            </table>

        </form>

        <jsp:include page="incl_playlist.jsp" />

            <table class="tracklist" cellspacing="0">
                <tr>
                    <th>
                        <fmt:message key="playlists" />
                    </th>
                    <c:if test="${!empty playlists}">
                        <th class="tracks" colspan="2">
                            <fmt:message key="tracks" />
                        </th>
                    </c:if>
                </tr>

        <c:if test="${!empty container}">
            <tr class="even">
                <td id="linkHomeFolder" class="homefolder" colspan="3" style="cursor:pointer" onclick="self.location.href='${servletUrl}/showPortal/${auth}'">
                    &nbsp;
                </td>
            </tr>
            <tr class="odd">
                <td id="linkParentFolder" class="parentfolder" colspan="3" style="cursor:pointer" onclick="self.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt>cid=${container.containerId}</mt:encrypt>'">
                    &nbsp;
                </td>
            </tr>
        </c:if>

        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td id="functionsDialogName${loopStatus.index}" class="${fn:toLowerCase(playlist.type)}" <c:if test="${playlist.type == 'ITunesFolder'}">style="cursor:pointer" onclick="self.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt>cid=${playlist.id}</mt:encrypt>'"</c:if>>
                    <c:choose>
                        <c:when test="${playlist.type != 'ITunesFolder' && playlist.trackCount >= 0}">
                            <a id="linkNameBrowseTrack${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>playlist=${playlist.id}/playlistName=${mtfn:encode64(playlist.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${playlist.name}" /></a>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${playlist.name}" />
                        </c:otherwise>
                    </c:choose>
                    <c:if test="${playlist.type == 'MyTunesSmart' && playlist.userOwner eq authUser.name}"><img id="linkRefreshSmartPlaylist${loopStatus.index}" style="vertical-align:middle;cursor:pointer" src="${themeUrl}/images/refresh.png" onclick="showLoading('<fmt:message key="loading.refreshSmartPlaylist"><fmt:param>${mtfn:escapeJs(playlist.name)}</fmt:param></fmt:message>');self.document.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt>refreshSmartPlaylistId=${playlist.id}</mt:encrypt>'"/></c:if>
                </td>
                <td class="tracks">
                    <c:choose>
                        <c:when test="${playlist.trackCount >= 0}">
                            <a id="linkButtonBrowseTrack${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt>playlist=${playlist.id}/playlistName=${mtfn:encode64(playlist.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${playlist.trackCount} </a>
                        </c:when>
                        <c:otherwise>
                            &nbsp;
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="actions">
                    <mttag:actions index="${loopStatus.index}"
                                   backUrl="${mtfn:encode64(backUrl)}"
                                   linkFragment="playlist=${playlist.id}"
                                   filename="${playlist.name}"
                                   zipFileCount="${playlist.trackCount}" />
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
        <c:set var="pagerCommand" scope="request">${servletUrl}/showPortal/${auth}/<mt:encrypt>currentListId=${currentListId}</mt:encrypt>/index={index}"</c:set>
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

        </div>
    </div>

    <div class="footer">
        <div class="inner">
            <c:if test="${!empty statistics && !globalConfig.disableBrowser && statistics.anyContent}">
                <fmt:message key="statistics" />:
                <c:set var="separator" value="" />
                <c:if test="${authUser.audio && statistics.musicCount > 0}">${statistics.musicCount} <fmt:message key="statistics.tracks" /><c:set var="separator" value="| "/></c:if>
                <c:if test="${authUser.audio && statistics.albumCount > 0}">${separator}<a id="linkStatsAlbum" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt>page=${config.browserStartIndex}</mt:encrypt>">${statistics.albumCount} <fmt:message key="statistics.albums"/></a><c:set var="separator" value="| "/></c:if>
                <c:if test="${authUser.audio && statistics.artistCount > 0}">${separator}<a id="linkStatsArtist" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt>page=${config.browserStartIndex}</mt:encrypt>">${statistics.artistCount} <fmt:message key="statistics.artists"/></a><c:set var="separator" value="| "/></c:if>
                <c:if test="${authUser.audio && statistics.genreCount > 0}">${separator}<a id="linkStatsGenre" href="${servletUrl}/browseGenre/${auth}/<mt:encrypt>page=${config.browserStartIndex}</mt:encrypt>">${statistics.genreCount} <fmt:message key="statistics.genres"/></a><c:set var="separator" value="| "/></c:if>
                <c:if test="${authUser.video && statistics.movieCount > 0}">${separator}<a id="linkStatsMovie" href="${servletUrl}/browseMovie/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">${statistics.movieCount} <fmt:message key="statistics.movies" /></a><c:set var="separator" value="| "/></c:if>
                <c:if test="${authUser.video && statistics.tvShowCount > 0}">${separator}<a id="linkStatsTvShow" href="${servletUrl}/browseTvShow/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">${statistics.tvShowCount} <fmt:message key="statistics.tvshows" /></a><c:set var="separator" value="| "/></c:if>
                <c:if test="${statistics.photoCount > 0 && authUser.photos}">${separator}<a id="linkStatsPhoto" href="${servletUrl}/browsePhotoAlbum/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>">${statistics.photoCount} <fmt:message key="statistics.photos" /></a></c:if>
            </c:if>
        </div>
    </div>

</div>

<jsp:include page="incl_select_flashplayer_dialog.jsp"/>
<jsp:include page="incl_functions_menu.jsp"/>

</body>

</html>
