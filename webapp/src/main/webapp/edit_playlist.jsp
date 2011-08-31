<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp" />

    <jsp:include page="incl_service_messages.jsp" />

    <script src="${appUrl}/js/prototype.js?ts=${sessionCreationTime}" type="text/javascript"></script>

    <script type="text/javascript">
        var unknownArtist = "<fmt:message key="unknownArtist"/>";
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
            loadView();
        });

        function loadView() {
            loadRows(0, itemsPerPage);
            refreshPager();
        }

        function refreshPager() {
            $jQ("#pager").empty().append(createPager());
        }

        function createTableRow(i, track) {
            var template = new Template($jQ("#templatePlaylistRow").text());
            return template.evaluate({
                trackId : track.id,
                index : i,
                indexBefore : i - 1,
                rowClass : (i % 2 == 0 ? "even" : "odd"),
                oddSuffix : (i % 2 == 0 ? "" : "_odd"),
                displayProtected : (track.protected ? "inline" : "none"),
                displayVideo : (track.mediaType == "Video" ? "inline" : "none"),
                trackName : track.name,
                trackArtist : track.artist != "!" ? track.artist : unknownArtist,
                displayMoveUp : firstItem + i > 0 ? "inline" : "none",
                displayMoveDown : firstItem + i + 1 < totalCount ? "inline" : "none"
            });
        }

        function swapTracks(index) {
            jsonRpc('${servletUrl}', "EditPlaylistService.moveTracks", [firstItem + index, 1, 1], function() {
                loadRows(index, 2);
            }, "${remoteApiSessionId}");
        }

        function removeTrack(index, id) {
            jsonRpc('${servletUrl}', "EditPlaylistService.removeTracks", [jQuery.makeArray([id])], function() {
                if (index == 0 && firstItem == totalCount - 1) {
                    firstItem -= itemsPerPage;
                    loadView();
                } else {
                    loadRows(index, itemsPerPage - index);
                }
            }, "${remoteApiSessionId}");
        }

        function loadRows(from, count) {
            jsonRpc('${servletUrl}', "EditPlaylistService.getPlaylist", [firstItem + from, count], function(result) {
                if (Math.floor((totalCount - 1) / itemsPerPage) != Math.floor((result.playlist.count - 1) / itemsPerPage)) {
                    totalCount = result.playlist.count;
                    refreshPager();
                } else {
                    totalCount = result.playlist.count;
                }
                if (result.playlist.userPrivate) {
                    $jQ("#privatePlaylist").attr("checked",  "checked");
                } else {
                    $jQ("#privatePlaylist").removeAttr("checked");
                }
                for (var i = from; i < from + count; i++) {
                    if (i >= 0 && i < itemsPerPage) {
                        if (firstItem + i >= totalCount) {
                            $jQ("#trackTableRow" + i).remove();
                        } else  {
                            var row = $jQ("#trackTableRow" + i);
                            if (row.size() == 0) {
                                $jQ("#trackTable > tbody").append(createTableRow(i, result.tracks[i - from]));
                            } else {
                                row.replaceWith(createTableRow(i, result.tracks[i - from]));
                            }
                        }
                    }
                }
            }, "${remoteApiSessionId}");
        }

        function swapRemoteCall(swapTopIndex, callback) {
            jsonRpc('${servletUrl}', "EditPlaylistService.moveTracks", [firstItem + swapTopIndex, 1, 1], callback, "${remoteApiSessionId}");
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
                displayPreviousControls : (firstPage > 0 ? "" : "display:none"),
                displayNextControls : ((firstPage + pagesPerPager) * itemsPerPage < totalCount ? "" : "display:none"),
                pagerPages : pageList
            });
        }

        function savePlaylist() {
            jsonRpc('${servletUrl}', "EditPlaylistService.savePlaylist", [$jQ("#playlistName").val(), $jQ("#privatePlaylist:checked").size() == 1], function(result, error) {
                if (error) {
                    $jQ("#errordialog p:first").empty().append(serviceMessages[error.msg]);
                    openDialog("#errordialog");
                } else {
                    document.location.href = "${servletUrl}/showPlaylistManager/${auth}";
                }
            }, "${remoteApiSessionId}");
        }

        function cancelEditPlaylist() {
            jsonRpc('${servletUrl}', "EditPlaylistService.cancelEditPlaylist", [], function() {
                document.location.href = "${servletUrl}/showPlaylistManager/${auth}";
            }, "${remoteApiSessionId}");
        }
    </script>

</head>

<body class="playlistEditor">

<div class="body">

	<div class="head">
	    <h1 class="manager">
	        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal" /></span></a>
	        <span><fmt:message key="myTunesRss" /></span>
	    </h1>
	</div>

	<div class="content">

		<div class="content-inner">

		    <ul class="menu">
		    	<li>
					<a id="linkBrowseArtist" href="${servletUrl}/browseArtist/${auth}/<mt:encrypt key="${encryptionKey}">page=${config.browserStartIndex}</mt:encrypt>">
						<fmt:message key="addMoreSongs" />
					</a>
		    	</li>
		        <li class="back">
		            <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back" /></a>
		        </li>
		    </ul>

		    <jsp:include page="/incl_error.jsp" />

		    <table class="settings" cellspacing="0">
		    	<tr>
		    		<td>
		    			<fmt:message key="playlistName" />
			    		<input type="text" id="playlistName" name="name" value="<c:out value="${editPlaylistName}"/>" />
						<input type="checkbox"
                               <c:if test="${!authUser.createPublicPlaylists}">disabled="disabled"</c:if>
						       id="privatePlaylist"
						       value="true"
						       <c:if test="${playlist.userPrivate}">checked="checked"</c:if> />
						       <fmt:message key="playlistUserPrivate" />
		    		</td>
		    	</tr>
		    </table>

		    <table id="trackTable" class="tracklist" cellspacing="0">
		        <tr>
		            <c:choose> <c:when test="${!empty tracks}">
		                <th class="active">&nbsp;</th>
		                <th class="active" colspan="3"><fmt:message key="playlistContent" /></th>
		            </c:when> <c:otherwise>
		                <th class="active" colspan="4"><fmt:message key="playlistContent" /></th>
		            </c:otherwise> </c:choose>
		        </tr>
		    </table>

		    <div id="pager" class="pager"></div>

		    <div class="buttons">
		        <input id="linkSave" type="button" onclick="savePlaylist()" value="<fmt:message key="savePlaylist"/>" />
		        <input id="linkCancel" type="button" onclick="cancelEditPlaylist()" value="<fmt:message key="doCancel"/>" />
		    </div>

		</div>

	</div>

	<div class="footer">
		<div class="inner"></div>
	</div>

</div>

<textarea id="templatePlaylistRow" style="display:none">
    <tr id="trackTableRow#{index}" class="#{rowClass}">
        <td class="iconleft">
            <a style="cursor:pointer;display:#{displayMoveUp}" onclick="swapTracks(#{indexBefore})"><img id="linkUp#{index}" src="${appUrl}/images/move_up.png" alt="U"/></a>
            <a style="cursor:pointer;display:#{displayMoveDown}" onclick="swapTracks(#{index})"><img id="linkDown#{index}" src="${appUrl}/images/move_down.png" alt="D"/></a>
        </td>
        <td>
            <img src="${appUrl}/images/protected#{oddSuffix}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle;display:#{displayProtected}" />
            <img src="${appUrl}/images/movie.png" alt="<fmt:message key="video"/>" style="vertical-align:middle;display:#{displayVideo}" />
            #{trackName}
        </td>
        <td>#{trackArtist}</td>
        <td class="actions">
            <a id="linkDelete#{index}" class="delete" onclick="removeTrack(#{index}, '#{trackId}')"><span>Delete</span></a>
        </td>
    </tr>
</textarea>

<textarea id="templatePager" style="display:none">
    <a id="linkPageFirst" style="cursor:pointer;#{displayPreviousControls}" onclick="firstItem=pagerGetIndexFirst();loadView()" class="first">First</a>
    <a id="linkPagePrev" style="cursor:pointer;#{displayPreviousControls}" onclick="firstItem=pagerGetIndexPrevious();loadView()" class="previous">Previous</a>
    #{pagerPages}
    <a id="linkPageNext" style="cursor:pointer;#{displayNextControls}" onclick="firstItem=pagerGetIndexNext();loadView()" class="next">Next</a>
    <a id="linnkPageLast" style="cursor:pointer;#{displayNextControls}" onclick="firstItem=pagerGetIndexLast();loadView()" class="last">Last</a>
</textarea>

<textarea id="templatePagerPage" style="display:none">
    <a id="linkPage#{pageName}" style="cursor:pointer" onclick="firstItem=#{index};loadView()" #{classActive}>#{pageName}</a>
</textarea>

<div id="errordialog" class="dialog">
    <h2>MyTunesRSS</h2>
    <div>
        <p>this is the error message</p>
        <p align="right">
            <button id="linkCloseError" onclick="$jQ.modal.close()"><fmt:message key="dialog.button.close"/></button>
        </p>
    </div>
</div>

</body>

</html>