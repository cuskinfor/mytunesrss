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

    <c:if test="${authUser.rss}">
        <c:forEach items="${playlists}" var="playlist">
            <link href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">playlist=${playlist.id}</mt:encrypt>/${mtfn:webSafeFileName(playlist.name)}.xml" rel="alternate" type="application/rss+xml" title="<c:out value="${playlist.name}" />" />
        </c:forEach>
    </c:if>

</head>

<body>

<div class="body">

    <h1 class="search" onclick="window.open('http://www.codewave.de')" style="cursor: pointer"><span><fmt:message key="myTunesRss" /></span></h1>

    <ul class="links">
        <c:if test="${authUser.editWebSettings}">
            <li><a href="${servletUrl}/showSettings/${auth}">
                <fmt:message key="doSettings" />
            </a></li>
        </c:if>
        <c:if test="${globalConfig.serverBrowserActive}">
            <li><a href="${servletUrl}/browseServers/${auth}">
                <fmt:message key="browseServers" />
            </a></li>
        </c:if>
        <c:if test="${empty globalConfig.autoLogin}">
            <li style="float:right"><a href="${servletUrl}/logout">
                <fmt:message key="doLogout" />
            </a></li>
        </c:if>
    </ul>

    <jsp:include page="/incl_error.jsp" />

    <form id="search" action="${servletUrl}/searchTracks/${auth}" method="post">

        <table class="portal" cellspacing="0">
            <tr>
                <td class="search">
                    <table border="0" cellspacing="0" cellpadding="0" style="border-bottom:0"><tr><td width="99%" style="background:transparent;padding:0 10px 0 0"><input class="text" type="text" name="searchTerm" value="<c:out value="${lastSearchTerm}"/>" style="width:99%"/></td><td style="background:transparent;padding:0 0"><input class="button" type="submit" value="<fmt:message key="doSearch"/>"/></td></tr></table>
                    <c:choose>
                        <c:when test="${authUser.searchFuzziness == -1}">
                            <table border="0" cellspacing="0" cellpadding="0" style="border-bottom:0"><tr>
                                <td style="background:transparent;padding:5px 10px 0 0">
                                    <select name="searchFuzziness" style="width:99%">
                                        <option value="0"><fmt:message key="search.fuzziness.0"/></option>
                                        <option value="30" <c:if test="${config.searchFuzziness == 30}">selected="selected"</c:if>><fmt:message key="search.fuzziness.30"/></option>
                                        <option value="60" <c:if test="${config.searchFuzziness == 60}">selected="selected"</c:if>><fmt:message key="search.fuzziness.60"/></option>
                                    </select>
                                </td>
                            </tr></table>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="searchFuzziness" value="${authUser.searchFuzziness}" />
                        </c:otherwise>
                    </c:choose>
                    <div>
                        <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
                    </div>
                </td>
                <td class="links">
                    <c:if test="${!globalConfig.disableBrowser}">
                        <a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>" style="background-image:url('${appUrl}/images/library_small.gif');">
                            <fmt:message key="browseLibrary" />
                        </a>
                    </c:if>
                    <c:if test="${authUser.createPlaylists}">
                        <c:choose>
                            <c:when test="${!stateEditPlaylist}">
                                <a href="${servletUrl}/showPlaylistManager/${auth}" style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                    <fmt:message key="managePlaylists" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${servletUrl}/showResource/${auth}/<mt:encrypt key="${encryptionKey}">resource=EditPlaylist/backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>"
                                   style="background-image:url('${appUrl}/images/feeds_small.gif');">
                                    <fmt:message key="finishPlaylist" />
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                    <c:if test="${uploadLink}">
                        <a href="${servletUrl}/showUpload/${auth}" style="background-image:url('${appUrl}/images/upload_small.gif');">
                            <fmt:message key="showUpload" />
                        </a>
                    </c:if>
                </td>
            </tr>
        </table>

    </form>

    <jsp:include page="incl_playlist.jsp" />

    <table cellspacing="0">
        <c:if test="${!empty statistics && !globalConfig.disableBrowser}">
            <tr>
                <th class="active" colspan="3" align="right">
                    <fmt:message key="statistics" />
                </th>
            </tr>
            <tr id="statistics">
                <td colspan="3">
                    <table class="statistics">
                        <tr>
                            <td><fmt:message key="statistics.tracks" />: ${statistics.trackCount}</td>
                            <td><a href="${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>"><fmt:message key="statistics.albums" />: ${statistics.albumCount}</a></td>
                            <td><a href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>"><fmt:message key="statistics.artists" />: ${statistics.artistCount}</a></td>
                            <td style="width:100%"><a href="${servletUrl}/browseGenre/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>"><fmt:message key="statistics.genres" />: ${statistics.genreCount}</a></td>
                        </tr>
                    </table>
                </td>
            </tr>
        </c:if>
        <tr>
            <th class="active">
                <fmt:message key="playlists" />
            </th>
            <c:if test="${!empty playlists}">
                <th class="active" colspan="2">
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
                    <c:out value="${playlist.name}" />
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
                <td class="icon">
                    <mttag:actions index="${loopStatus.index}"
                                   backUrl="${mtfn:encode64(backUrl)}"
                                   linkFragment="playlist=${playlist.id}"
                                   filename="${mtfn:webSafeFileName(playlist.name)}"
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
        <c:set var="pagerCommand" scope="request" value="${servletUrl}/showPortal/${auth}/index={index}" />
        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
        <jsp:include page="incl_bottomPager.jsp" />
    </c:if>

</div>

<jsp:include page="incl_functions_menu.jsp" />

<div id="editTagsDialog" style="display:none">
    <input id="editTagsDialog_newTag" />
</div>

<script type="text/javascript">
    $jQ("#editTagsDialog_newTag").autocomplete("${servletUrl}/getTagsForAutocomplete");
    $jQ("#editTagsDialog").dialog({
        autoOpen:false,
        modal:true
    });
    function openEditTagsDialog() {
        //$jQ("#editTagsDialog").dialog("option", "keyword", keyword);
        $jQ("#editTagsDialog").dialog("open");
    }
</script>

</body>

</html>
