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

        var imageUrls = new Array(
                <c:forEach items="${tracks}" var="track" varStatus="trackLoopStatus">
                    "<c:if test="${track.imageCount > 0}">${servletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/size=32</mt:encrypt></c:if>"<c:if test="${!trackLoopStatus.last}">,</c:if>
                </c:forEach>
        );

        var itemsPerPage = 10;
        var pagesPerPager = 10;
        var currentPage = 0;
        var featureSet;

        function createPlaylist() {
            unhighlightAllTracks();
            var start = currentPage * itemsPerPage;
            for (var i = 0; i < itemsPerPage; i++) {
                if (start + i < trackNames.length) {
                    document.getElementById("trackrow" + i).style.display = "table-row";
                    document.getElementById("track" + i).innerHTML = trackNames[start + i];
                    if (imageUrls[start + i] != "") {
                        document.getElementById("cover" + i).innerHTML = "<img src=\"" + imageUrls[start + i] + "\"/>";
                    } else {
                        document.getElementById("cover" + i).innerHTML = "&nbsp;";
                    }
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
            if (trackNames.length <= itemsPerPage) {
                document.getElementById("pager").style.display = "none";
            } else {
                document.getElementById("pager").style.display = "block";
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
        }

        function startPlayback(index) {
            unhighlightAllTracks();
            jsonRpc('${servletUrl}', 'RemoteControlService.play', [currentPage * itemsPerPage + index + 1]);
        }

        function highlightTrack(index) {
            document.getElementById("trackrow" + index).className = "remoteplayback";
        }

        function unhighlightAllTracks() {
            for (var i = 0; i < itemsPerPage; i++) {
                document.getElementById("trackrow" + i).className = (i % 2 == 1 ? "even" : "odd");
            }
        }

        function updateInterface(trackInfo) {
            var firstTrackOnPage = itemsPerPage * currentPage;
            var highlightIndex = trackInfo.currentTrack - firstTrackOnPage - 1;

            unhighlightAllTracks();

            if (trackInfo.playing && highlightIndex >= 0 && highlightIndex < itemsPerPage) {
                highlightTrack(highlightIndex);
            }

            if (trackInfo.playing) {
                $("rc_play").style.display = "none";
                $("rc_pause").style.display = "inline";
            } else if (!trackInfo.playing) {
                $("rc_play").style.display = "inline";
                $("rc_pause").style.display = "none";
            }

            var percentage = trackInfo.currentTime * 100 / trackInfo.length;
            if (percentage > 0 && percentage <= 100) {
                document.getElementById("progressBar").style.width = (percentage) + "%";
            } else {
                document.getElementById("progressBar").style.width = 0;
            }
        }

        function getStateAndUpdateInterface() {
            jsonRpc('${servletUrl}', 'RemoteControlService.getCurrentTrackInfo', [], updateInterface);
        }

        function play() {
            jsonRpc('${servletUrl}', 'RemoteControlService.play', [0]);
            getStateAndUpdateInterface();
        }

        function pause() {
            jsonRpc('${servletUrl}', 'RemoteControlService.pause', []);
            getStateAndUpdateInterface();
        }

        function stop() {
            jsonRpc('${servletUrl}', 'RemoteControlService.stop', []);
            getStateAndUpdateInterface();
        }

        function nextTrack() {
            jsonRpc('${servletUrl}', 'RemoteControlService.next', []);
            getStateAndUpdateInterface();
        }

        function previousTrack() {
            jsonRpc('${servletUrl}', 'RemoteControlService.prev', []);
            getStateAndUpdateInterface();
        }

        function registerObserver() {
            if (featureSet.jumpTo) {
                Event.observe("progressBackground", "click", function(event) {
                    if (event.isLeftClick()) {
                        var containerLeft = Position.page($("progressBackground"))[0];
                        var containerTop = Position.page($("progressBackground"))[1];
                        var mouseX = event.pointerX();
                        var mouseY = event.pointerY();
                        var horizontalPosition = mouseX - containerLeft;
                        var verticalPosition = mouseY - containerTop;
                        var containerDimensions = $('progressBackground').getDimensions();
                        var height = containerDimensions.height;
                        var width = containerDimensions.width;
                        if (horizontalPosition >= 0 && verticalPosition >= 0 && mouseX <= (width + containerLeft) && mouseY <= (height + containerTop) ) {
                            jsonRpc('${servletUrl}', 'RemoteControlService.jumpTo', [Math.round(horizontalPosition * 100 / width)]);
                            getStateAndUpdateInterface();
                        }
                    }
                });
            }
        }

        function init() {
            jsonRpc('${servletUrl}', 'RemoteControlService.getFeatures', [], function(features) {
                featureSet = features;
                if (featureSet.timeInfo) {
                    $("progressDiv").style.display = "block";
                    registerObserver();
                }
            });
            new PeriodicalExecuter(function() {
                getStateAndUpdateInterface();
            }, 2);
            createPlaylist();
            <c:choose>
                <c:when test="${!empty param.album}">
                    jsonRpc('${servletUrl}', 'RemoteControlService.loadAlbum', ['${fn:replace(mtfn:decode64(param.album), "'", "\\'")}', true]);
                </c:when>
                <c:when test="${!empty param.artist}">
                    jsonRpc('${servletUrl}', 'RemoteControlService.loadArtist', ['${fn:replace(mtfn:decode64(param.artist), "'", "\\'")}', ${param.fullAlbums == "true"} ,true]);
                </c:when>
                <c:when test="${!empty param.genre}">
                    jsonRpc('${servletUrl}', 'RemoteControlService.loadGenre', ['${fn:replace(mtfn:decode64(param.genre), "'", "\\'")}', true]);
                </c:when>
                <c:when test="${!empty param.playlist}">
                    jsonRpc('${servletUrl}', 'RemoteControlService.loadPlaylist', ['${fn:replace(param.playlist, "'", "\\'")}', true]);
                </c:when>
                <c:when test="${!empty param.tracklist}">
                    jsonRpc('${servletUrl}', 'RemoteControlService.loadTracks', [['${fn:join(fn:split(param.tracklist, ","), "','")}'], true]);
                </c:when>
                <c:otherwise>
                    jsonRpc('${servletUrl}', 'RemoteControlService.loadTrack', ['${fn:replace(param.track, "'", "\\'")}', true]);
                </c:otherwise>
            </c:choose>
        }

    </script>

</head>

<body onload="init()">

    <div class="body">

        <h1 class="search" style="cursor:pointer" onclick="self.document.location.href='${mtfn:decode64(param.backUrl)}'"><span><fmt:message key="myTunesRss" /></span></h1>

        <table cellspacing="0">

            <c:forEach begin="0" end="9" varStatus="itemLoopStatus">
                <tr id="trackrow${itemLoopStatus.index}" class="${cwfn:choose(itemLoopStatus.count % 2 == 0, 'even', 'odd')}">
                    <td id="cover${itemLoopStatus.index}" class="remotecontrolTrackImage">&nbsp;</td>
                    <td style="cursor:pointer" onclick="startPlayback(${itemLoopStatus.index})" class="artist" id="track${itemLoopStatus.index}"/>
                </tr>
            </c:forEach>

        </table>

        <div id="pager" class="pager">

            <img id="pager_first" src="${appUrl}/images/pager_first.gif" alt="first" style="cursor:pointer" onclick="currentPage = 0;createPlaylist()"/>
            <img id="pager_previous" src="${appUrl}/images/pager_previous.gif" alt="previous" style="cursor:pointer" onclick="currentPage--;createPlaylist()"/>

            <c:forEach begin="0" end="9" varStatus="status">
                <a id="pager_active_${status.index}" class="active">&nbsp;</a>
                <a id="pager_inactive_${status.index}" style="cursor:pointer" onclick="currentPage = (Math.floor(currentPage / pagesPerPager) * pagesPerPager) + ${status.index};createPlaylist()">&nbsp;</a>
            </c:forEach>

            <img id="pager_next" src="${appUrl}/images/pager_next.gif" alt="next" style="cursor:pointer" onclick="currentPage++;createPlaylist()"/>
            <img id="pager_last" src="${appUrl}/images/pager_last.gif" alt="last" style="cursor:pointer" onclick="currentPage = Math.floor(trackNames.length / itemsPerPage);createPlaylist()"/>

        </div>


        <div class="remotecontrolPanel">
            <img src="${appUrl}/images/rc_prev.png" alt="prev" onclick="previousTrack()" style="cursor:pointer"/>
            <img id="rc_play" src="${appUrl}/images/rc_play.png" alt="prev" onclick="play()" style="cursor:pointer;display:none"/>
            <img id="rc_pause" src="${appUrl}/images/rc_pause.png" alt="prev" onclick="pause()" style="cursor:pointer"/>
            <img src="${appUrl}/images/rc_stop.png" alt="stop" onclick="stop()" style="cursor:pointer"/>
            <img src="${appUrl}/images/rc_next.png" alt="next" onclick="nextTrack()" style="cursor:pointer"/>
        </div>

        <div id="progressDiv" style="display:none">
            <div id="progressBackground">
                <div id="progressBar">&nbsp;</div>
            </div>
        </div>

    </div>

</body>

</html>
