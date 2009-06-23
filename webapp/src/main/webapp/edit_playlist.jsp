<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl">${servletUrl}/editPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=${param.allowEditEmpty}/index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}
</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp" />

    <script type="text/javascript">
        $jQ(document).ready(function() {
            $jQ("#trackTable").sortable({
                axis:'y',
                items:'tr',
                update:function(event, ui) {
                    var first = ui.item.attr("id").split("_")[1];
                    var offset = ui.item.prevAll().length - first;
                    jsonRpc('${servletUrl}', 'EditPlaylistService.moveTracks', [${config.effectivePageSize * param.index} + first, 1, offset], fixTableRows, '${remoteApiSessionId}');
                }
            });
        });

        function fixTableRows() {
            var rows = $jQ("#trackTable").find("tr").attr("class", function(i) {
                return i % 2 == 0 ? "even" : "odd";
            });
            var rows = $jQ("#trackTable").find("tr").attr("id", function(i) {
                return "trackTableItem_" + i;
            });
        }

        function removeTrack(trackId, index) {
            jsonRpc('${servletUrl}', 'EditPlaylistService.removeTracks', [$A([trackId])], function() {
                var rowToDelete = $jQ("#trackTable").find("tr")[index].remove();
                jsonRpc('${servletUrl}', 'EditPlaylistService.getPlaylist', [${config.effectivePageSize * (param.index + 1)}, 1], function(result) {
                    if (result.tracks != undefined) {
                        alert(result.tracks[0]);
                    }
                    fixTableRows();
                }, '${remoteApiSessionId}');
            }, '${remoteApiSessionId}');

        }
    </script>

</head>

<body>

<div class="body">

    <h1 class="manager">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal" /></a> <span><fmt:message key="myTunesRss" /></span>
    </h1>

    <c:if test="${states.addToPlaylistMode}">
        <ul class="links">
            <li style="float:right;">
                <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back" /></a>
            </li>
        </ul>
    </c:if>

    <jsp:include page="/incl_error.jsp" />

    <form id="playlist" action="${servletUrl}/savePlaylist/${auth}" method="post">
        <table class="portal" cellspacing="0">
            <tr>
                <td class="playlistManager">
                    <fmt:message key="playlistName" /> <input type="text" name="name" value="<c:out value="${editPlaylistName}"/>" />
                </td>
                <td class="links">
                    <a class="add"
                       href="${servletUrl}/continuePlaylist/${auth}"
                       style="background-image:url('${appUrl}/images/add_more.gif');"><fmt:message key="addMoreSongs" /></a>
                </td>
        </table>

        <input type="hidden" name="backUrl" value="${param.backUrl}" /> <input type="hidden" name="allowEditEmpty" value="${param.allowEditEmpty}" />
        <table cellspacing="0">
            <tr>
                <th class="active" colspan="3"><fmt:message key="playlistSettings" /></th>
            </tr>
            <tr>
                <td class="even" colspan="3">
                    <input type="checkbox"
                           name="user_private"
                           value="true"
                           <c:if test="${playlist.userPrivate}">checked="checked"</c:if> /> <fmt:message key="playlistUserPrivate" />
                </td>
            </tr>
            <c:if test="${!states.addToPlaylistMode}">
                <tr>
                    <td colspan="3" style="padding:0">
                        <c:set var="displayFilterUrl"
                               scope="request">${servletUrl}/editPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=${param.allowEditEmpty}</mt:encrypt>/index=${param.index}/backUrl=${param.backUrl}
                        </c:set> <c:set var="filterTypeActive" scope="request" value="true" /> <c:set var="filterProtectionActive"
                                                                                                      scope="request"
                                                                                                      value="true" />
                        <jsp:include page="/incl_display_filter.jsp" />
                    </td>
                </tr>
            </c:if>
            <tr>
                <th class="active" colspan="3"><fmt:message key="playlistContent" /></th>
            </tr>
        </table>
        <table cellspacing="0" id="trackTable">
            <c:forEach items="${tracks}" var="track" varStatus="trackLoop">
                <tr id="trackTableItem_${trackLoop.index}" class="${cwfn:choose(trackLoop.index % 2 == 0, 'even', 'odd')}">
                    <td>
                        <c:if test="${track.protected}"><img src="${appUrl}/images/protected${cwfn:choose(trackLoop.index % 2 == 0, '', '_odd')}.gif"
                                                             alt="<fmt:message key="protected"/>"
                                                             style="vertical-align:middle" /></c:if>
                        <c:if test="${track.mediaType.jspName == 'Video'}"><img src="${appUrl}/images/movie${cwfn:choose(trackLoop.index % 2 == 0, '', '_odd')}.gif"
                                                         alt="<fmt:message key="video"/>"
                                                         style="vertical-align:middle" /></c:if>
                        <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknown, track.name)}" />
                    </td>
                    <td>
                        <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
                    </td>
                    <td class="icon">
                        <a onclick="removeTrack('${track.id}', ${trackLoop.index})"><img src="${appUrl}/images/delete${cwfn:choose(trackLoop.index % 2 == 0, '', '_odd')}.gif" alt="delete" /></a>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${!empty pager}">
            <c:set var="pagerCommand" scope="request">${servletUrl}/editPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=${param.allowEditEmpty}/backUrl=${param.backUrl}</mt:encrypt>/index={index}</c:set>
            <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
            <jsp:include page="incl_bottomPager.jsp" />
        </c:if>

        <div class="buttons">
            <input type="submit"
                   onclick="document.forms['playlist'].action = '${servletUrl}/savePlaylist/${auth}';document.forms['playlist'].elements['backUrl'].value = '${mtfn:encode64(backUrl)}'"
                   value="<fmt:message key="savePlaylist"/>"/>
            <input type="button"
                   onclick="document.location.href = '${servletUrl}/cancelEditPlaylist/${auth}/backUrl=${param.backUrl}'"
                   value="<fmt:message key="doCancel"/>"/>
        </div>
    </form>

</div>

</body>

</html>