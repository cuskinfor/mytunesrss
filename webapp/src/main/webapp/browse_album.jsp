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

<%--@elvariable id="albums" type="java.util.List"--%>
<%--@elvariable id="stateEditPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="editablePlaylists" type="java.util.List"--%>
<%--@elvariable id="simpleNewPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="albumPager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="indexPager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="singleArtist" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="singleGenre" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="msgUnknown" type="java.lang.String"--%>
<%--@elvariable id="allArtistGenreTrackCount" type="java.lang.Integer"--%>
<%--@elvariable id="allAlbumsTrackCount" type="java.lang.Integer"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${albums}" var="album">
            <c:choose>
                <c:when test="${mtfn:unknown(album.name)}">
                    <c:set var="albumName"><fmt:message key="unknown"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="albumName" value="${album.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/${mtfn:virtualAlbumName(album)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${albumName}" />" />
        </c:forEach>
    </c:if>

</head>

<body class="browse">

    <div class="body">
    
        <div class="head">
            <h1 class="artists">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>

        <jsp:include page="/incl_error.jsp" />
        
        <div class="content">
        
            <div class="content-inner">

                <ul class="menu">
                    <li>
                        <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${cwfn:choose(empty param.artist, param.page, '1')}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
                    </li>
                    <li>
                        <a href="${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseGenres"/></a>
                    </li>
                    <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
                        <li class="playlist">
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
                    <c:if test="${!empty param.backUrl}">
                        <li class="back">
                            <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                        </li>
                    </c:if>
                </ul>

                <jsp:include page="incl_playlist.jsp" />
                
                <c:if test="${param.sortByYear != 'true'}">
                    <c:set var="pager" scope="request" value="${albumPager}" />
                    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseAlbum/${auth}/page={index}" />
                    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.artist || !empty param.genre, '*', param.page)}" />
                    <c:set var="filterToggle" scope="request" value="true" />
                    <jsp:include page="incl_pager.jsp" />
                </c:if>

    <table class="tracklist" cellspacing="0">
        <tr>
            <td colspan="4" style="padding:0">
                <c:set var="displayFilterUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index=${param.index}/backUrl=${param.backUrl}</c:set>
                <c:set var="filterYearActive" scope="request" value="true"/>
                <jsp:include page="/incl_display_filter.jsp"/>
            </td>
        </tr>
        <tr>
            <th class="active">
                <c:if test="${!empty param.genre}">${mtfn:capitalize(mtfn:decode64(param.genre))}</c:if>
                <fmt:message key="albums"/>
                <c:if test="${!empty param.artist}"> <fmt:message key="with"/> "${cwfn:choose(mtfn:unknown(mtfn:decode64(param.artist)), msgUnknown, mtfn:decode64(param.artist))}"</c:if>
            </th>
            <th><fmt:message key="artist"/></th>
            <th><fmt:message key="tracks"/></th>
            <th>&nbsp;</th>
        </tr>
        <c:forEach items="${albums}" var="album" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td id="functionsDialogName${loopStatus.index}" <c:if test="${config.showThumbnailsForAlbums && !empty(album.imageHash)}">class="coverThumbnailColumn"</c:if>">
                    <div class="trackName">
                        <c:if test="${config.showThumbnailsForAlbums && !empty(album.imageHash)}">
                            <img class="coverThumbnail" id="albumthumb_${loopStatus.index}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${cwfn:encodeUrl(album.imageHash)}/size=32</mt:encrypt>" onmouseover="showTooltip(this)" onmouseout="hideTooltip(this)" alt=""/>
                            <div class="tooltip" id="tooltip_albumthumb_${loopStatus.index}"><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${cwfn:encodeUrl(album.imageHash)}/size=${config.albumImageSize}</mt:encrypt>" alt=""/></div>
                        </c:if>
                        <c:choose>
                            <c:when test="${mtfn:unknown(album.name)}">
                                <fmt:message key="unknown"/>
                            </c:when>
                            <c:otherwise>
                                <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${album.name}"/></a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
                <td>
                    <c:choose>
                        <c:when test="${album.artistCount == 1}">
                            <c:choose>
                                <c:when test="${singleArtist}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(album.artist), msgUnknown, album.artist)}" />
                                </c:when>
                                <c:otherwise>
                                    <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(album.artist))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                        <c:out value="${cwfn:choose(mtfn:unknown(album.artist), msgUnknown, album.artist)}" /></a>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise><fmt:message key="variousArtists"/></c:otherwise>
                    </c:choose>
                </td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${album.trackCount} </a>
                </td>
                <td class="actions">
                    <c:choose>
                        <c:when test="${!stateEditPlaylist}">
                            <mttag:actions index="${loopStatus.index}"
                                           backUrl="${mtfn:encode64(backUrl)}"
                                           linkFragment="album=${cwfn:encodeUrl(mtfn:encode64(album.name))}"
                                           filename="${mtfn:virtualAlbumName(album)}"
                                           zipFileCount="${album.trackCount}"
                                           externalSitesFlag="${mtfn:externalSites('album') && !mtfn:unknown(album.name) && authUser.externalSites}"
                                           editTagsType="Album"
                                           editTagsId="${album.name}" />

                        </c:when>
                        <c:otherwise>
                            <a style="cursor:pointer" onclick="addAlbumsToPlaylist($A(['${mtfn:escapeJs(album.name)}']))">
                                <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add"/> </a>
                            <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(mtfn:encode64(album.name))}/name=${cwfn:encodeUrl(album.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/one_click_playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="oneClickPlaylist" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${(singleArtist || singleGenre) && fn:length(albums) > 1}">
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 0, 'even', 'odd')}">
                <td colspan="2" id="functionsDialogName${fn:length(albums) + 1}"><em>
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><fmt:message key="allTracksOfAboveAlbums"/></a>
                </em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${allAlbumsTrackCount}</a>
                </td>
                <td class="actions">
                    <c:choose>
                        <c:when test="${!stateEditPlaylist}">
                            <mttag:actions index="${fn:length(albums) + 1}"
                                           backUrl="${mtfn:encode64(backUrl)}"
                                           linkFragment="fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}"
                                           filename="${mtfn:webSafeFileName(mtfn:decode64(param.artist))}"
                                           zipFileCount="${allAlbumsTrackCount}" />
                        </c:when>
                        <c:otherwise>
                            <a style="cursor:pointer" onclick="addToPlaylist(null, $A(['${mtfn:escapeJs(cwfn:decode64(param.artist))}']), $A(['${mtfn:escapeJs(cwfn:decode64(param.genre))}']), null, true)">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                            <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">fullAlbums=true/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/name=${cwfn:encodeUrl(param.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/one_click_playlist${cwfn:choose(fn:length(albums) % 2 == 0, '', '_odd')}.gif" alt="oneClickPlaylist" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <tr class="${cwfn:choose(fn:length(albums) % 2 == 0, 'odd', 'even')}">
                <td colspan="2" id="functionsDialogName${fn:length(albums) + 2}"><em>
                    <c:choose>
                        <c:when test="${singleArtist}">
                            <mt:array var="params">
                                <mt:arrayElement value="${mtfn:decode64(param.artist)}"/>
                            </mt:array>
                            <fmt:message var="localizedMessage" key="allTracksOfArtist"/>
                        </c:when>
                        <c:otherwise>
                            <mt:array var="params">
                                <mt:arrayElement value="${mtfn:decode64(param.genre)}"/>
                            </mt:array>
                            <fmt:message var="localizedMessage" key="allTracksOfGenre"/>
                        </c:otherwise>
                    </c:choose>
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${cwfn:message(localizedMessage, params)}"/></a>
                </em></td>
                <td class="tracks">
                    <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">${allArtistGenreTrackCount}</a>
                </td>
                <td class="actions">
                    <c:choose>
                        <c:when test="${!stateEditPlaylist}">
                            <mttag:actions index="${fn:length(albums) + 2}"
                                           backUrl="${mtfn:encode64(backUrl)}"
                                           linkFragment="artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/fullAlbums=false"
                                           filename="${mtfn:webSafeFileName(mtfn:decode64(param.artist))}"
                                           zipFileCount="${allArtistGenreTrackCount}" />
                        </c:when>
                        <c:otherwise>
                            <a style="cursor:pointer" onclick="addToPlaylist(null, $A(['${mtfn:escapeJs(cwfn:decode64(param.artist))}']), $A(['${mtfn:escapeJs(cwfn:decode64(param.genre))}']), null, false)">
                                <img src="${appUrl}/images/add${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif" alt="add" /> </a>
                            <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}/name=${cwfn:encodeUrl(param.artist)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                <img src="${appUrl}/images/one_click_playlist${cwfn:choose(fn:length(albums) % 2 == 0, '_odd', '')}.gif" alt="oneClickPlaylist" /> </a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:if>
    </table>

    <c:if test="${!empty indexPager}">
        <c:set var="pager" scope="request" value="${indexPager}" />
        <c:set var="pagerCommand" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/artist=${cwfn:encodeUrl(param.artist)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>
    
        </div>
    
    </div>
    
    <div class="footer">
        <div class="footer-inner"></div>
    </div>        

</div>

<jsp:include page="incl_edit_playlist_dialog.jsp"/>

<c:set var="externalSiteDefinitions" scope="request" value="${mtfn:externalSiteDefinitions('album')}"/>
<jsp:include page="incl_external_sites_dialog.jsp"/>
<jsp:include page="incl_functions_menu.jsp" />
<jsp:include page="incl_edit_tags.jsp" />

</body>

</html>
