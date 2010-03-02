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

<%--@elvariable id="playlists" type="java.util.List"--%>

<c:set var="backUrl" scope="request">${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <link rel="search" type="application/opensearchdescription+xml" href="${servletUrl}/openSearch?username=${mtfn:encode64(authUser.name)}&auth=${mtfn:encode64(auth)}" title="MyTunesRSS" />

    <c:if test="${authUser.rss}">
        <c:forEach items="${playlists}" var="playlist">
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${playlist.name}" />" />
        </c:forEach>
    </c:if>

</head>

<body onload="document.forms[0].elements['searchTerm'].focus()" class="startpage">

<div class="body">

    <div class="head">
        <h1 onclick="window.open('http://www.codewave.de')" style="cursor: pointer"><span><fmt:message key="myTunesRss" /></span></h1>
    </div>

    <div class="content">
        <div class="content-inner">

        <ul class="menu">
            <c:if test="${authUser.editWebSettings}">
                <li class="settings first"><a href="${servletUrl}/showSettings/${auth}">
                    <fmt:message key="doSettings" />
                </a></li>
            </c:if>
            <c:if test="${globalConfig.serverBrowserActive}">
                <li class="servers <c:if test="${!authUser.editWebSettings}">first</c:if>"><a href="${servletUrl}/browseServers/${auth}">
                    <fmt:message key="browseServers" />
                </a></li>
            </c:if>
            <c:if test="${authUser.editWebSettings || globalConfig.serverBrowserActive}">
	            <li class="spacer">&nbsp;</li>
            </c:if>
            <c:if test="${empty globalConfig.autoLogin}">
                <li class="logout"><a href="${servletUrl}/logout">
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
                    <input class="button" type="submit" value="<fmt:message key="doSearch"/>"/>
	                </td>

	                <td class="links">
                    <c:if test="${!globalConfig.disableBrowser}">
                        <a class="library" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>">
                            <fmt:message key="browseLibrary" />
                        </a>
                    </c:if>
                    <c:if test="${authUser.createPlaylists}">
                        <c:choose>
                            <c:when test="${!stateEditPlaylist}">
                                <a class="playlists" href="${servletUrl}/showPlaylistManager/${auth}">
                                    <fmt:message key="managePlaylists" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a class="playlists" href="${servletUrl}/showResource/${auth}/<mt:encrypt key="${encryptionKey}">resource=EditPlaylist/backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>"
                                   style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                    <fmt:message key="finishPlaylist" />
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${uploadLink}">
                        <a class="upload" href="${servletUrl}/showUpload/${auth}">
                            <fmt:message key="showUpload" />
                        </a>
                    </c:if>
	                </td>
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
                <td class="homefolder" colspan="3" style="cursor:pointer" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}'">
                    [ / ]
                </td>
            </tr>
            <tr class="odd">
                <td class="parentfolder" colspan="3" style="cursor:pointer" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">cid=${container.containerId}</mt:encrypt>'">
                    [ .. ]
                </td>
            </tr>
        </c:if>

        <c:forEach items="${playlists}" var="playlist" varStatus="loopStatus">
            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                <td id="functionsDialogName${loopStatus.index}" class="${fn:toLowerCase(playlist.type)}" <c:if test="${playlist.type == 'ITunesFolder'}">style="cursor:pointer" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}/<mt:encrypt key="${encryptionKey}">cid=${playlist.id}</mt:encrypt>'"</c:if>>
                    <c:choose>
                        <c:when test="${playlist.type != 'ITunesFolder' && playlist.trackCount >= 0}">
                            <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><c:out value="${playlist.name}" /></a>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${playlist.name}" />
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="tracks">
                    <c:choose>
                        <c:when test="${playlist.trackCount >= 0}">
                            <a href="${servletUrl}/browseTrack/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"> ${playlist.trackCount} </a>
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
                                   filename="${mtfn:webSafeFileName(playlist.name)}"
                                   zipFileCount="${playlist.trackCount}"
                                   editTagsType="Playlist"
                                   editTagsId="${playlist.id}" />
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
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPortal/${auth}/index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

        </div>
    </div>

    <div class="footer">
        <div class="inner">
            <c:if test="${!empty statistics && !globalConfig.disableBrowser}">
                <fmt:message key="statistics" />:
                ${statistics.trackCount} <fmt:message key="statistics.tracks" />,
                <a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>">${statistics.albumCount} <fmt:message key="statistics.albums" /></a>,
                <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>">${statistics.artistCount} <fmt:message key="statistics.artists" /></a>,
                <a href="${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>">${statistics.genreCount} <fmt:message key="statistics.genres" /></a>
               </c:if>
        </div>
    </div>

</div>

<jsp:include page="incl_functions_menu.jsp" />
<jsp:include page="incl_edit_tags.jsp" />

</body>

</html>
