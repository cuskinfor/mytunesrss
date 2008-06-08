<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        var trackNames = new Array(
            <c:forEach items="${tracks}" var="track" varStatus="trackLoopStatus">
                "<c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" /> - <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknown, track.name)}" />"<c:if test="${!trackLoopStatus.last}">,</c:if>
            </c:forEach>
        );
        var trackLinks = new Array(
            <c:forEach items="${tracks}" var="track" varStatus="trackLoopStatus">
                "${mtfn:makeHttp(servletUrl)}/playTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(track.id)}/tc=${mtfn:tcParamValue(config, authUser, track)}/playerRequest=${param.playerRequest}</mt:encrypt>/${mtfn:virtualTrackName(track)}.${mtfn:suffix(config, authUser, track)}"<c:if test="${!trackLoopStatus.last}">,</c:if>
            </c:forEach>
        );
        var itemsPerPage = 10;
        var pagesPerPager = 10;
        var currentPage = 0;

        function createPlayer(index) {
            var firstTrack = (currentPage * itemsPerPage) + index;
            var iframe = top.frames.player;
            iframe.document.write("<html><body style='border:0;margin:0;padding:0'>");
            iframe.document.write("<object classid='clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B' width='100%' height='16' codebase='http://www.apple.com/qtactivex/qtplugin.cab' id='jukebox'>");
            iframe.document.write("<param name='controller' value='true' />");
            iframe.document.write("<param name='autoplay' value='true' />");
            iframe.document.write("<param name='kioskmode' value='true' />");
            iframe.document.write("<param name='postdomevents' value='true' />");
            iframe.document.write("<param name='type' value='video/quicktime' />");
            iframe.document.write("<embed id='embed_jukebox' src='" + trackLinks[firstTrack] + "' controller='true' autoplay='true' kioskmode='true' width='100%' height='16' type='video/quicktime' postdomevents='true' name='jukebox'");
            for (var i = firstTrack + 1; i < trackLinks.length; i++) {
                iframe.document.write(" qtnext" + (i - 1) + "='<" + trackLinks[i] + ">T<myself>'");
            }
            iframe.document.write("></embed>");
            iframe.document.write("</object>");
            iframe.document.write("</body></html>");
            iframe.document.close();
            if (document.addEventListener) {
                getPlayer().addEventListener("qt_play", playbackStarted, false);
                getPlayer().addEventListener("qt_ended", playbackEnded, false);
                getPlayer().addEventListener("qt_paused", playbackEnded, false);
            } else {
                getPlayer().attachEvent("on_qt_play", playbackStarted);
                getPlayer().attachEvent("on_qt_ended", playbackEnded);
                getPlayer().attachEvent("on_qt_paused", playbackEnded);
            }
        }

        function createPlaylist() {
            var start = currentPage * itemsPerPage;
            for (var i = 0; i < itemsPerPage; i++) {
                if (start + i < trackNames.length) {
                    document.getElementById("trackrow" + i).style.display = "table-row";
                    document.getElementById("track" + i).innerHTML = trackNames[start + i];
                } else {
                    document.getElementById("trackrow" + i).style.display = "none";
                }
            }
            if (currentPage > 0) {
                document.getElementById("pager_first").style.display = "inline";
                document.getElementById("pager_previous").style.display = "inline";
            } else {
                document.getElementById("pager_first").style.display = "none";
                document.getElementById("pager_previous").style.display = "none";
            }
            if ((currentPage + 1) * itemsPerPage < trackNames.length) {
                document.getElementById("pager_next").style.display = "inline";
                document.getElementById("pager_last").style.display = "inline";
            } else {
                document.getElementById("pager_next").style.display = "none";
                document.getElementById("pager_last").style.display = "none";
            }
            start = Math.floor(currentPage / pagesPerPager) * pagesPerPager;
            for (i = 0; i < pagesPerPager; i++) {
                if ((start + i) * itemsPerPage < trackNames.length) {
                    if (start + i == currentPage) {
                        document.getElementById("pager_active_" + i).innerHTML = (start + i);
                        document.getElementById("pager_active_" + i).style.display = "inline";
                        document.getElementById("pager_inactive_" + i).style.display = "none";
                    } else {
                        document.getElementById("pager_inactive_" + i).innerHTML = (start + i);
                        document.getElementById("pager_active_" + i).style.display = "none";
                        document.getElementById("pager_inactive_" + i).style.display = "inline";
                    }
                } else {
                    document.getElementById("pager_active_" + i).style.display = "none";
                    document.getElementById("pager_inactive_" + i).style.display = "none";
                }
            }
        }

        function getPlayer() {
            var obj = top.frames.player.document.getElementById("jukebox");
            if (!obj) {
                obj = top.frames.player.document.getElementById("embed_jukebox");
            }
            return obj;
        }

        function playbackStarted() {
            var url = top.frames.player.document.jukebox.GetURL();
            unhighlightAllTracks();
            for (var i = 0; i < trackLinks.length; i++) {
                if (decodeURI(url) == decodeURI(trackLinks[i])) {
                    currentPage = Math.floor(i / itemsPerPage);
                    createPlaylist();
                    highlightTrack(i - (currentPage * itemsPerPage));
                    break;
                }
            }
        }

        function playbackEnded() {
            unhighlightAllTracks();
        }

        function highlightTrack(index) {
            document.getElementById("trackrow" + index).className = "qtplayback";
        }

        function unhighlightAllTracks() {
            for (var i = 0; i < itemsPerPage; i++) {
                document.getElementById("trackrow" + i).className = (i % 2 == 1 ? "even" : "odd");
            }
        }

    </script>

</head>

<body onload="createPlaylist();createPlayer(0)">

    <div class="body">

        <h1 class="search"><span><fmt:message key="myTunesRss" /></span></h1>

        <table cellspacing="0">

            <c:forEach begin="0" end="9" varStatus="itemLoopStatus">
                <tr id="trackrow${itemLoopStatus.index}" class="${cwfn:choose(itemLoopStatus.count % 2 == 0, 'even', 'odd')}" style="cursor:pointer" onclick="createPlayer(${itemLoopStatus.index})">
                    <td class="artist" id="track${itemLoopStatus.index}"/>
                </tr>
            </c:forEach>

        </table>

        <div class="pager">

            <img id="pager_first" src="${appUrl}/images/pager_first.gif" alt="first" style="cursor:pointer" onclick="currentPage = 0;createPlaylist()"/>
            <img id="pager_previous" src="${appUrl}/images/pager_previous.gif" alt="previous" style="cursor:pointer" onclick="currentPage--;createPlaylist()"/>

            <c:forEach begin="0" end="9" varStatus="status">
                <a href="#" id="pager_active_${status.index}" class="active">&nbsp;</a>
                <a href="#" id="pager_inactive_${status.index}" style="cursor:pointer" onclick="currentPage = (Math.floor(currentPage / pagesPerPager) * pagesPerPager) + ${status.index};createPlaylist()">&nbsp;</a>
            </c:forEach>

            <img id="pager_next" src="${appUrl}/images/pager_next.gif" alt="next" style="cursor:pointer" onclick="currentPage++;createPlaylist()"/>
            <img id="pager_last" src="${appUrl}/images/pager_last.gif" alt="last" style="cursor:pointer" onclick="currentPage = Math.floor(trackNames.length / itemsPerPage);createPlaylist()"/>

        </div>

        <iframe id="player" name="player" style="width:100%;height:32px;border:0;margin:5px 0 5px 0;padding:0"/>

    </div>

</body>

</html>
