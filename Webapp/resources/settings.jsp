<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

<div class="body">

    <h1 class="settings">
        <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal" /></a> <span><fmt:message key="myTunesRss" /></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <form action="${servletUrl}/saveSettings" method="post">
        <table cellspacing="0">
            <tr>
                <th class="active"><fmt:message key="settings.info" /></th>
                <th>&nbsp;</th>
            </tr>
            <mt:initFlipFlop value1="" value2="class=\"odd\""/>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.username" /></td>
                <td><c:out value="${authUser.name}"/></td>
            </tr>
            <c:if test="${authUser.quota}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.quota" /></td>
                    <td><c:out value="${mtfn:memory(authUser.bytesQuota)}"/> ${authUser.quotaType}</td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.quotaRemain" /></td>
                    <td><c:out value="${mtfn:memory(authUser.quotaRemaining)}"/></td>
                </tr>
            </c:if>
            <tr>
                <th class="active"><fmt:message key="settings" /></th>
                <th>&nbsp;</th>
            </tr>
            <mt:initFlipFlop value1="" value2="class=\"odd\""/>
            <c:if test="${authUser.changePassword}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.password" /></td>
                    <td>
                        <input type="password"
                               name="password1"
                               maxlength="30"
                               value="<c:out value="${param.password1}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.retypePassword" /></td>
                    <td>
                        <input type="password"
                               name="password2"
                               maxlength="30"
                               value="<c:out value="${param.password2}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
            </c:if>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.itemsPerPage" /></td>
                <td><input type="text"
                           name="pageSize"
                           maxlength="3"
                           value="<c:out value="${cwfn:choose(config.pageSize > 0, config.pageSize, '')}"/>"
                           style="width: 50px;" /></td>
            </tr>
            <c:if test="${authUser.rss}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.rssFeedLimit" /></td>
                    <td>
                        <input type="text"
                               name="rssFeedLimit"
                               maxlength="3"
                               value="<c:out value="${cwfn:choose(config.rssFeedLimit > 0, config.rssFeedLimit, '')}"/>"
                               style="width: 50px;" />
                    </td>
                </tr>
            </c:if>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.randomPlaylistSize" /></td>
                <td>
                    <input type="text"
                           name="randomPlaylistSize"
                           maxlength="3"
                           value="<c:out value="${cwfn:choose(config.randomPlaylistSize > 0, config.randomPlaylistSize, '')}"/>"
                           style="width: 50px;" />
                </td>
            </tr>
            <c:if test="${authUser.rss}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.playlistTypes.rss" /></td>
                    <td>
                        <input type="checkbox" name="feedType" value="rss" <c:if test="${config.showRss}">checked="checked"</c:if> />
                        <img src="${appUrl}/images/rss_odd.gif" alt="RSS" style="vertical-align:text-top;" />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.playlist}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.playlistTypes.playlist" /></td>
                    <td>
                        <input type="checkbox" name="feedType" value="playlist" <c:if test="${config.showPlaylist}"> checked="checked"</c:if> />
                        <img src="${appUrl}/images/playlist_odd.gif" alt="playlist" style="vertical-align:text-top;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.playlistType" /></td>
                    <td>
                        <select name="playlistType">
                            <option value="M3u" <c:if test="${config.playlistType eq 'M3u'}">selected="selected"</c:if>>m3u</option>
                            <option value="Xspf" <c:if test="${config.playlistType eq 'Xspf'}">selected="selected"</c:if>>xspf</option>
                        </select>
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.download}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.showDownload" /></td>
                    <td>
                        <input type="checkbox" name="showDownload" value="true" <c:if test="${config.showDownload}">checked="checked"</c:if> />
                        <img src="${appUrl}/images/download.gif" alt="playlist" style="vertical-align:text-top;" />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.player}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.showPlayer" /></td>
                    <td>
                        <input type="checkbox" name="showPlayer" value="true" <c:if test="${config.showPlayer}">checked="checked"</c:if> />
                        <img src="${appUrl}/images/player.gif" alt="player" style="vertical-align:text-top;" />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.rss}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.rssArtwork" /></td>
                    <td>
                        <input type="checkbox" name="rssArtwork" value="true" <c:if test="${config.rssArtwork}">checked="checked"</c:if> />
                    </td>
                </tr>
            </c:if>
        </table>

        <div class="buttons">
            <input type="submit" value="<fmt:message key="doSave"/>" />
            <input type="button" value="<fmt:message key="doCancel"/>" onclick="document.location.href='${servletUrl}/showPortal'" />
        </div>
    </form>
</div>

</body>

</html>
