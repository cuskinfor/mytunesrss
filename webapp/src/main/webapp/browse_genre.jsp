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

<%--@elvariable id="genres" type="java.util.List"--%>
<%--@elvariable id="stateEditPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="editablePlaylists" type="java.util.List"--%>
<%--@elvariable id="simpleNewPlaylist" type="java.lang.Boolean"--%>
<%--@elvariable id="genrePager" type="de.codewave.mytunesrss.Pager"--%>
<%--@elvariable id="indexPager" type="de.codewave.mytunesrss.Pager"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <c:if test="${authUser.rss}">
        <c:forEach items="${genres}" var="genre">
            <c:choose>
                <c:when test="${mtfn:unknown(genre.name)}">
                    <c:set var="genrename"><fmt:message key="unknownGenre"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="genrename" value="${genre.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">genre=${mtfn:encode64(genre.name)}</mt:encrypt>/${mtfn:virtualGenreName(genre)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${genrename}" />" />
        </c:forEach>
    </c:if>

</head>

<body class="browse">

    <div class="body">

        <div class="head">
            <h1>
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>

        <div class="content">

            <div class="content-inner">

                <ul class="menu">
                    <li class="first">
                        <a id="linkBrowseArtist" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
                    </li>
                    <li>
                        <a id="linkBrowseAlbum" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseAlbums"/></a>
                    </li>
					<li class="active">
						<span><fmt:message key="browseGenres"/></span>
					</li>
                    <c:if test="${!stateEditPlaylist && authUser.createPlaylists}">
                        <li>
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
                </ul>

                <jsp:include page="/incl_error.jsp" />

                <jsp:include page="incl_playlist.jsp" />

                <c:set var="pager" scope="request" value="${genrePager}" />
                <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseGenre/${auth}/page={index}" />
                <c:set var="pagerCurrent" scope="request" value="${param.page}" />
                <c:set var="filterToggle" scope="request" value="false" />
                <jsp:include page="incl_pager.jsp" />

                <form id="browse" action="" method="post">

        			<fieldset>
                        <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
        			</fieldset>

                    <table class="tracklist" cellspacing="0">
                        <tr>
                            <th class="active">
                                <fmt:message key="genres"/>
                            </th>
                            <th><fmt:message key="albums"/></th>
                            <th><fmt:message key="artists"/></th>
                            <th><fmt:message key="tracks"/></th>
                            <th class="actions">&nbsp;</th>
                        </tr>
                        <c:forEach items="${genres}" var="genre" varStatus="loopStatus">
                            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                                <td id="functionsDialogName${loopStatus.index}" class="genre">
                                    <c:out value="${genre.name}" />
                                </td>
                                <td class="album">
                                    <a id="linkAlbumsForGenre${loopStatus.index}" href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">genre=${mtfn:encode64(genre.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.albumCount} </a>
                                </td>
                                <td class="genreartist">
                                    <a id="linkArtistsForGenre${loopStatus.index}" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${mtfn:encode64(genre.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.artistCount} </a>
                                </td>
                                <td class="tracks">
                                    <a id="linkTracksForGenre${loopStatus.index}" href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">genre=${mtfn:encode64(genre.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.trackCount} </a>
                                </td>
                                <td class="actions">
                                    <c:choose>
                                        <c:when test="${!stateEditPlaylist}">
                                            <mttag:actions index="${loopStatus.index}"
                                                           backUrl="${mtfn:encode64(backUrl)}"
                                                           linkFragment="genre=${mtfn:encode64(genre.name)}"
                                                           filename="${mtfn:virtualGenreName(genre)}"
                                                           zipFileCount="${genre.trackCount}"
                                                           defaultPlaylistName="${genre.name}"
                                                           shareText="${genre.name}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${globalConfig.flashPlayer && authUser.player && config.showPlayer}">
                                                <a id="linkEditPlaylistFlash${loopStatus.index}" class="flash" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=<mt:encode64>genre=${mtfn:encode64(genre.name)}/filename=${mtfn:virtualGenreName(genre)}.xspf</mt:encode64></mt:encrypt>/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                                            </c:if>
                                            <a id="linkAddToPlaylist${loopStatus.index}" class="add" onclick="addGenresToPlaylist(jQuery.makeArray(['${mtfn:escapeJs(genre.name)}']))" title="<fmt:message key="playlist.addGenre"/>"><span><fmt:message key="playlist.addGenre"/></span></a>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>

                    <c:if test="${!empty indexPager}">
                        <c:set var="pager" scope="request" value="${indexPager}" />
                        <c:set var="pagerCommand" scope="request">${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>/index={index}</c:set>
                        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
                        <jsp:include page="incl_bottomPager.jsp" />
                    </c:if>

                </form>

            </div>

        </div>

        <div class="footer">
            <div class="inner"></div>
        </div>

    </div>

    <jsp:include page="incl_select_flashplayer_dialog.jsp"/>
    <jsp:include page="incl_edit_playlist_dialog.jsp"/>
    <jsp:include page="incl_functions_menu.jsp"/>

</body>

</html>
