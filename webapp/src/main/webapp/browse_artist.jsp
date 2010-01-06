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

<%--@elvariable id="artists" type="java.util.List"--%>
<%--@elvariable id="stateEditPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="editablePlaylists" type="java.util.List"--%>
<%--@elvariable id="simpleNewPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="artistPager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="indexPager" type="de.codewave.mytunesrss.Pager"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">album=${cwfn:encodeUrl(param.album)}/genre=${cwfn:encodeUrl(param.genre)}/page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${artists}" var="artist">
            <c:choose>
                <c:when test="${mtfn:unknown(artist.name)}">
                    <c:set var="artistname"><fmt:message key="unknown"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="artistname" value="${artist.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(artist.name))}</mt:encrypt>/${mtfn:virtualArtistName(artist)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${artistname}" />" />
        </c:forEach>
    </c:if>

</head>

<body class="browse">

    <div class="body">
    
        <div class="head">    
            <h1 class="artist">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>
        
        <div class="content">
        
            <div class="content-inner">
    
                <jsp:include page="/incl_error.jsp" />
    
                <ul class="menu">
                    <li>
                        <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${cwfn:choose(empty param.album, param.page, '1')}</mt:encrypt>"><fmt:message key="browseAlbums"/></a>
                    </li>
                    <li>
                        <a href="${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseGenres"/></a>
                    </li>
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
                    <c:if test="${!empty param.backUrl}">
                        <li style="float:right;">
                            <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                        </li>
                    </c:if>
                </ul>
            
                <jsp:include page="incl_playlist.jsp" />
            
                <c:set var="pager" scope="request" value="${artistPager}" />
                <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseArtist/${auth}/page={index}" />
                <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.album || !empty param.genre, '*', param.page)}" />
                <c:set var="filterToggle" scope="request" value="true" />
                <jsp:include page="incl_pager.jsp" />
                
                <c:set var="displayFilterUrl" scope="request">${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/album=${cwfn:encodeUrl(param.album)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index=${param.index}/backUrl=${param.backUrl}</c:set>
                <jsp:include page="/incl_display_filter.jsp"/>
            
                <table class="tracklist" cellspacing="0">
                        <th class="active">
                            <c:if test="${!empty param.genre}">${mtfn:capitalize(mtfn:decode64(param.genre))}</c:if>
                            <fmt:message key="artists"/>
                            <c:if test="${!empty param.album}"> <fmt:message key="on"/> "<c:out value="${mtfn:decode64(param.album)}" />"</c:if>
                        </th>
                        <th><fmt:message key="albums"/></th>
                        <th><fmt:message key="tracks"/></th>
                        <th>&nbsp;</th>
                    </tr>
                    <c:forEach items="${artists}" var="artist" varStatus="loopStatus">
                        <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                            <td id="functionsDialogName${loopStatus.index}" class="artist">
                                <c:choose>
                                    <c:when test="${mtfn:unknown(artist.name)}">
                                        <fmt:message key="unknown"/>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(artist.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${artist.name}" /></a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="album">
                                <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(artist.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${artist.albumCount} </a>
                            </td>
                            <td class="tracks">
                                <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(artist.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${artist.trackCount} </a>
                            </td>
                            <td class="actions">
                                <c:choose>
                                    <c:when test="${!stateEditPlaylist}">
                                        <mttag:actions index="${loopStatus.index}"
                                                       backUrl="${mtfn:encode64(backUrl)}"
                                                       linkFragment="artist=${cwfn:encodeUrl(mtfn:encode64(artist.name))}/fullAlbums=false"
                                                       filename="${mtfn:virtualArtistName(artist)}"
                                                       zipFileCount="${artist.trackCount}"
                                                       externalSitesFlag="${mtfn:externalSites('artist') && !mtfn:unknown(artist.name) && authUser.externalSites}"
                                                       editTagsType="Artist"
                                                       editTagsId="${artist.name}" />
                                    </c:when>
                                    <c:otherwise>
                                        <a style="cursor:pointer" onclick="addArtistsToPlaylist($A(['${mtfn:escapeJs(artist.name)}']), false)">
                                            <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                                        <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(mtfn:encode64(artist.name))}/name=${cwfn:encodeUrl(artist.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                            <img src="${appUrl}/images/one_click_playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            
                <c:if test="${!empty indexPager}">
                    <c:set var="pager" scope="request" value="${indexPager}" />
                    <c:set var="pagerCommand" scope="request">${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/album=${cwfn:encodeUrl(param.album)}/genre=${cwfn:encodeUrl(param.genre)}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
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
    
    <c:set var="externalSiteDefinitions" scope="request" value="${mtfn:externalSiteDefinitions('artist')}"/>
    <jsp:include page="incl_external_sites_dialog.jsp"/>
    <jsp:include page="incl_functions_menu.jsp" />
    <jsp:include page="incl_edit_tags.jsp" />

</body>

</html>
