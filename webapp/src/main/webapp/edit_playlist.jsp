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
        var firstItem = 0;
        var itemsPerPage = ${config.effectivePageSize};
        var pagesPerPager = 10;
        var totalCount = 0;
        function pagerGetIndexFirst() {
            return 0;
        }
        function pagerGetIndexPrevious() {
            return (pagerGetFirstPage() - 1) * itemsPerPage;
        }
        function pagerGetIndexNext() {
            return (pagerGetFirstPage() + pagesPerPager) * itemsPerPage;
        }
        function pagerGetIndexLast() {
            return Math.floor((totalCount - 1) / itemsPerPage) * itemsPerPage;
        }
        function pagerGetFirstPage() {
            return Math.floor(pagerGetCurrentPage() / pagesPerPager) * pagesPerPager;
        }
        function pagerGetCurrentPage() {
            return Math.floor(firstItem / itemsPerPage);
        }

        $jQ(document).ready(function() {
            loadPlaylist();
        });

        function loadPlaylist() {
            $jQ("#pager").empty();
            jsonRpc('${servletUrl}', "EditPlaylistService.getPlaylist", [firstItem, ${config.effectivePageSize}], function(result) {
                $jQ("#trackTable > tbody > tr:has(td.editPlaylistMoveUp)").remove();
                totalCount = result.playlist.count;
                for (var i = 0; i < result.tracks.length; i++) {
                    $jQ("#trackTable > tbody").append(createTableRow(i, result.tracks[i]));
                }
                setClickHandlers();
                $jQ("#pager").append(createPager());
            }, "${remoteApiSessionId}");
        }

        function createTableRow(i, track) {
            var template = new Template($jQ("#templatePlaylistRow").text());
            return template.evaluate({
                trackId : track.id,
                rowClass : (i % 2 == 0 ? "even" : "odd"),
                oddSuffix : (i % 2 == 0 ? "" : "_odd"),
                displayProtected : (track.protected ? "inline" : "none"),
                displayVideo : (track.mediaType == "Video" ? "inline" : "none"),
                trackName : track.name,
                trackArtist : track.artist,
                displayMoveUp : firstItem + i > 0 ? "inline" : "none",
                displayMoveDown : firstItem + i + 1 < totalCount ? "inline" : "none"
            });
        }

        function setClickHandlers() {
            $jQ("td.editPlaylistMoveUp > a").unbind("click");
            $jQ("td.editPlaylistMoveDown > a").unbind("click");
            $jQ("td.editPlaylistMoveUp:not(:first) > a").click(function() {
                swapRows($jQ(this).closest("tr").prev(), $jQ(this).closest("tr"));
            });
            $jQ("td.editPlaylistMoveUp:first > a").click(function() {
                swapRemoteCall($jQ(this).closest("tr").prev(), $jQ(this).closest("tr"));
                replaceRow($jQ(this).closest("tr"), firstItem - 1, 0);
            });
            $jQ("td.editPlaylistMoveDown:not(:last) > a").click(function() {
                swapRows($jQ(this).closest("tr"), $jQ(this).closest("tr").next());
            });
            $jQ("td.editPlaylistMoveDown:last > a").click(function() {
                swapRemoteCall($jQ(this).closest("tr"), $jQ(this).closest("tr").next());
                replaceRow($jQ(this).closest("tr"), firstItem + itemsPerPage, ${config.effectivePageSize - 1});
            });
        }

        function swapRemoteCall(first, second, callback) {
            jsonRpc('${servletUrl}', "EditPlaylistService.moveTracks", [first.prevAll("tr.even,tr.odd").length, 1, 1], callback, "${remoteApiSessionId}");
        }

        function replaceRow(row, indexToLoad, indexInList) {
            jsonRpc('${servletUrl}', "EditPlaylistService.getPlaylist", [indexToLoad, 1], function(result) {
                row.replaceWith(createTableRow(indexInList, result.tracks[0]));
                setClickHandlers();
            }, "${remoteApiSessionId}");
        }

        function swapRows(first, second) {
            swapRemoteCall(first, second)
            first.insertAfter(second);
            switchOddEven(first);
            switchOddEven(second);
            var firstUpLink = first.find(".editPlaylistMoveUp > a");
            var firstDownLink = first.find(".editPlaylistMoveDown > a");
            var secondUpLink = second.find(".editPlaylistMoveUp > a");
            var secondDownLink = second.find(".editPlaylistMoveDown > a");
            if (firstUpLink.css("display") == "none") {
                firstUpLink.css("display", "inline");
                secondUpLink.css("display", "none");
            }
            if (secondDownLink.css("display") == "none") {
                secondDownLink.css("display", "inline");
                firstDownLink.css("display", "none");
            }
            setClickHandlers();
        }

        function switchOddEven(element) {
            if (element.hasClass("even")) {
                element.removeClass("even");
                element.addClass("odd");
            } else {
                element.removeClass("odd");
                element.addClass("even");
            }
            element.find("img").attr("src", function(i) {
                var url = element.find("img:eq(" + i + ")").attr("src");
                if (url.endsWith("_odd.gif")) {
                    return url.truncate(url.length - 4, ".gif");
                } else {
                    return url.truncate(url.length - 4, "") + "_odd.gif";
                }
            });
        }

        function createPager() {
            var templatePager = new Template($jQ("#templatePager").text());
            var templatePagerCommand = new Template($jQ("#templatePagerCommand").text());
            var templatePagerPage = new Template($jQ("#templatePagerPage").text());
            var currentPage = Math.floor(firstItem / itemsPerPage);
            var firstPage = Math.floor(currentPage / pagesPerPager) * pagesPerPager;
            var pageList = "";
            for (var i = firstPage; i < firstPage + pagesPerPager && i * itemsPerPage < totalCount; i++) {
                pageList += templatePagerPage.evaluate({
                    index : (i * itemsPerPage),
                    classActive : (i == currentPage ? "class=\"active\"" : ""),
                    pageName : i + 1
                });
            }
            return templatePager.evaluate({
                displayPreviousControls : (firstPage > 0 ? "inline" : "none"),
                displayNextControls : ((firstPage + pagesPerPager) * itemsPerPage < totalCount ? "inline" : "none"),
                pagerPages : pageList
            });
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
        <table id="trackTable" cellspacing="0">
            <tr>
                <th class="active" colspan="6"><fmt:message key="playlistSettings" /></th>
            </tr>
            <tr>
                <td class="even" colspan="6">
                    <input type="checkbox"
                           name="user_private"
                           value="true"
                           <c:if test="${playlist.userPrivate}">checked="checked"</c:if> /> <fmt:message key="playlistUserPrivate" />
                </td>
            </tr>
            <c:if test="${!states.addToPlaylistMode}">
                <tr>
                    <td colspan="6" style="padding:0">
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
                <c:choose> <c:when test="${!empty tracks}">
                    <th class="check"><input type="checkbox" name="none" value="none" onclick="selectAll('item', '${trackIds}', this)" /></th>
                    <th class="active" colspan="2">&nbsp;</th>
                    <th class="active" colspan="3"><fmt:message key="playlistContent" /></th>
                </c:when> <c:otherwise>
                    <th class="active" colspan="6"><fmt:message key="playlistContent" /></th>
                </c:otherwise> </c:choose>
            </tr>
        </table>

        <div id="pager" class="pager"></div>

        <div class="buttons">
            <input type="button"
                   onclick="document.forms['playlist'].action = '${servletUrl}/removeFromPlaylist/${auth}';document.forms['playlist'].submit()"
                   value="<fmt:message key="removeSelected"/>" /> <input type="submit"
                                                                         onclick="document.forms['playlist'].action = '${servletUrl}/savePlaylist/${auth}';document.forms['playlist'].elements['backUrl'].value = '${mtfn:encode64(backUrl)}'"
                                                                         value="<fmt:message key="savePlaylist"/>" /> <input type="button"
                                                                                                                             onclick="document.location.href = '${servletUrl}/cancelEditPlaylist/${auth}/backUrl=${param.backUrl}'"
                                                                                                                             value="<fmt:message key="doCancel"/>" />
        </div>
    </form>

</div>

<textarea id="templatePlaylistRow" style="display:none">
    <tr class="#{rowClass}">
        <td class="check">
            <input type="checkbox" id="item#{trackId}" name="track" value="#{trackId}" />
        </td>
        <td class="editPlaylistMoveUp">
            <a style="cursor:pointer;display:#{displayMoveUp}"><img src="${appUrl}/images/move_up#{oddSuffix}.gif" alt="U"/></a>
        </td>
        <td class="editPlaylistMoveDown">
            <a style="cursor:pointer;display:#{displayMoveDown}"><img src="${appUrl}/images/move_down#{oddSuffix}.gif" alt="D"/></a>
        </td>
        <td width="99%">
            <img src="${appUrl}/images/protected#{oddSuffix}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle;display:#{displayProtected}" />
            <img src="${appUrl}/images/movie#{oddSuffix}.gif" alt="<fmt:message key="video"/>" style="vertical-align:middle;display:#{displayVideo}" />
            #{trackName}
        </td>
        <td>#{trackArtist}</td>
        <td class="icon">
            <a href="${servletUrl}/removeFromPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=${param.allowEditEmpty}/track=${track.id}</mt:encrypt>/backUrl=${param.backUrl}">
                <img src="${appUrl}/images/delete#{oddSuffix}.gif" alt="delete" /> </a>
        </td>
    </tr>
</textarea>

<textarea id="templatePager" style="display:none">
    <a style="cursor:pointer;display:#{displayPreviousControls}" onclick="firstItem=pagerGetIndexFirst();loadPlaylist()"><img src="${appUrl}/images/pager_first.gif" alt="first"/></a>
    <a style="cursor:pointer;display:#{displayPreviousControls}" onclick="firstItem=pagerGetIndexPrevious();loadPlaylist()"><img src="${appUrl}/images/pager_previous.gif" alt="previous"/></a>
    #{pagerPages}
    <a style="cursor:pointer;display:#{displayNextControls}" onclick="firstItem=pagerGetIndexNext();loadPlaylist()"><img src="${appUrl}/images/pager_next.gif" alt="next"/></a>
    <a style="cursor:pointer;display:#{displayNextControls}" onclick="firstItem=pagerGetIndexLast();loadPlaylist()"><img src="${appUrl}/images/pager_last.gif" alt="last"/></a>
</textarea>

<textarea id="templatePagerPage" style="display:none">
    <a style="cursor:pointer" onclick="firstItem=#{index};loadPlaylist()" #{classActive}>#{pageName}</a>
</textarea>
</body>

</html>