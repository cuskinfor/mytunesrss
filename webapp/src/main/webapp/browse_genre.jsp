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
                    <c:set var="genrename"><fmt:message key="unknown"/></c:set>
                </c:when>
                <c:otherwise>
                    <c:set var="genrename" value="${genre.name}"/>
                </c:otherwise>
            </c:choose>
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/${mtfn:virtualGenreName(genre)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${genrename}" />" />
        </c:forEach>
    </c:if>

</head>

<body>

<div class="body">

    <h1 class="browse">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <ul class="links">
        <li>
            <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseArtist"/></a>
        </li>
        <li>
            <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>"><fmt:message key="browseAlbums"/></a>
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
    </ul>

    <jsp:include page="incl_playlist.jsp" />

    <c:set var="pager" scope="request" value="${genrePager}" />
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseGenre/${auth}/page={index}" />
    <c:set var="pagerCurrent" scope="request" value="${param.page}" />
    <jsp:include page="incl_pager.jsp" />

    <form id="browse" action="" method="post">

			<fieldset>
            <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
			</fieldset>

        <table class="select" cellspacing="0">
            <tr>
                <th class="active">
                    <fmt:message key="genres"/>
                </th>
                <th><fmt:message key="albums"/></th>
                <th><fmt:message key="artists"/></th>
                <th colspan="2"><fmt:message key="tracks"/></th>
            </tr>
            <c:forEach items="${genres}" var="genre" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td id="functionsDialogName${loopStatus.index}" class="genre">
                        <c:out value="${genre.name}" />
                    </td>
                    <td class="album">
                        <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.albumCount} </a>
                    </td>
                    <td class="genreartist">
                        <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.artistCount} </a>
                    </td>
                    <td class="tracks">
                        <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${genre.trackCount} </a>
                    </td>
                    <td class="icon">
                        <c:choose>
                            <c:when test="${!stateEditPlaylist}">
                                <mttag:actions index="${loopStatus.index}"
                                               backUrl="${mtfn:encode64(backUrl)}"
                                               linkFragment="genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}"
                                               filename="${mtfn:virtualGenreName(genre)}"
                                               zipFileCount="${genre.trackCount}" />
                            </c:when>
                            <c:otherwise>
                                <a style="cursor:pointer" onclick="addGenresToPlaylist($A(['${mtfn:escapeJs(genre.name)}']), false)">
                                    <img src="${appUrl}/images/add${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
                                <a href="${servletUrl}/createOneClickPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">genre=${cwfn:encodeUrl(mtfn:encode64(genre.name))}/name=${cwfn:encodeUrl(genre.name)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}">
                                    <img src="${appUrl}/images/one_click_playlist${cwfn:choose(loopStatus.index % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
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

<jsp:include page="incl_edit_playlist_dialog.jsp"/>
<jsp:include page="incl_functions_menu.jsp" />

</body>

</html>
