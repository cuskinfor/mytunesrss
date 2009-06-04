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

    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0" />

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
        var myFullScreen = false;
        var sessionId;

        function createPlaylist() {
            unhighlightAllTracks();
            var start = currentPage * itemsPerPage;
            for (var i = 0; i < itemsPerPage; i++) {
                if (start + i < trackNames.length) {
                    try {
                        document.getElementById("trackrow" + i).style.display = "table-row";
                    } catch (e) {
                        document.getElementById("trackrow" + i).style.display = "block";
                    }
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

        function startPlayback(index, callback) {
            unhighlightAllTracks();
            execJsonRpc('RemoteControlService.play', [currentPage * itemsPerPage + index], callback);
        }

        function highlightTrack(index, className) {
            document.getElementById("trackrow" + index).className = className;
        }

        function unhighlightAllTracks() {
            for (var i = 0; i < itemsPerPage; i++) {
                document.getElementById("trackrow" + i).className = (i % 2 == 1 ? "even" : "odd");
            }
        }

        function updateInterface(trackInfo) {
            if (trackInfo == null || trackInfo == undefined) {
                return;
            }

            var firstTrackOnPage = itemsPerPage * currentPage;
            var highlightIndex = trackInfo.currentTrack - firstTrackOnPage - 1;

            unhighlightAllTracks();

            if (highlightIndex >= 0 && highlightIndex < itemsPerPage) {
                highlightTrack(highlightIndex, trackInfo.playing ? "remoteplaybackplaying" : "remoteplayback");
            }

            if (trackInfo.playing) {
                $("rc_play").style.display = "none";
                $("rc_pause").style.display = "inline";
            } else if (!trackInfo.playing) {
                $("rc_play").style.display = "inline";
                $("rc_pause").style.display = "none";
            }

            var percentage = trackInfo.currentTime != -1 && trackInfo.length > -1 ? trackInfo.currentTime * 100 / trackInfo.length : 0;
            if (percentage > 0 && percentage <= 100) {
                document.getElementById("progressBar").style.width = (percentage) + "%";
            } else {
                document.getElementById("progressBar").style.width = 0;
            }

            if (trackInfo.volume > 0 && trackInfo.volume <= 100) {
                document.getElementById("volumeBar").style.width = (trackInfo.volume) + "%";
            } else {
                document.getElementById("volumeBar").style.width = 0;
            }
        }

        function getStateAndUpdateInterface() {
            execJsonRpc('RemoteControlService.getCurrentTrackInfo', [], updateInterface);
        }

        function play() {
            execJsonRpc('RemoteControlService.play', [-1], getStateAndUpdateInterface);
        }

        function pause() {
            execJsonRpc('RemoteControlService.pause', [], getStateAndUpdateInterface);
        }

        function stop() {
            execJsonRpc('RemoteControlService.stop', [], getStateAndUpdateInterface);
        }

        function nextTrack() {
            execJsonRpc('RemoteControlService.next', [], getStateAndUpdateInterface);
        }

        function previousTrack() {
            execJsonRpc('RemoteControlService.prev', [], getStateAndUpdateInterface);
        }

        function shuffle() {
            self.document.location.href = '${servletUrl}/showRemoteControl/${auth}/shuffle=true/backUrl=${param.backUrl}/fullScreen=' + myFullScreen;
        }

        function toggleFullScreen() {
            execJsonRpc('RemoteControlService.setFullScreen', [!myFullScreen], function(result) {
                myFullScreen = result;
            });
        }

        function handleProgressBar(event) {
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
                execJsonRpc('RemoteControlService.jumpTo', [Math.round(horizontalPosition * 100 / width)], getStateAndUpdateInterface);
            }
        }

        function handleVolumeBar(event) {
            var containerLeft = Position.page($("volumeBackground"))[0];
            var containerTop = Position.page($("volumeBackground"))[1];
            var mouseX = event.pointerX();
            var mouseY = event.pointerY();
            var horizontalPosition = mouseX - containerLeft;
            var verticalPosition = mouseY - containerTop;
            var containerDimensions = $('volumeBackground').getDimensions();
            var height = containerDimensions.height;
            var width = containerDimensions.width;
            if (horizontalPosition >= 0 && verticalPosition >= 0 && mouseX <= (width + containerLeft) && mouseY <= (height + containerTop) ) {
                execJsonRpc('RemoteControlService.setVolume', [Math.round(horizontalPosition * 100 / width)], getStateAndUpdateInterface);
            }
        }

        function registerObservers() {
            Event.observe("progressBackground", "mousedown", function(event) {
                if (event.isLeftClick()) {
                    handleProgressBar(event);
                }
            });
            Event.observe("volumeBackground", "mousedown", function(event) {
                if (event.isLeftClick()) {
                    handleVolumeBar(event);
                }
            });
        }

        function init() {
            createPlaylist();
            execJsonRpc('RemoteControlService.getCurrentTrackInfo', [], init2);
        }

        function init2(trackInfo) {
            registerObservers();
            new PeriodicalExecuter(function() {
                getStateAndUpdateInterface();
            }, 2);
            if (!trackInfo.playing) {
                <c:choose>
                    <c:when test="${param.fullScreen == 'true'}">
                        startPlayback(0, function() {
                            toggleFullScreen();
                        })
                    </c:when>
                    <c:otherwise>
                        startPlayback(0);
                    </c:otherwise>
                </c:choose>
            }
        }

        function execJsonRpc(method, params, callback) {
            if (sessionId == null || sessionId == undefined) {
                jsonRpc('${servletUrl}', 'LoginService.login', ['${authUser.name}', '${authUser.hexEncodedPasswordHash}', 1], function(loginResult) {
                    sessionId = loginResult;
                    jsonRpc('${servletUrl}', method, params, callback, sessionId);
                })
            }
            jsonRpc('${servletUrl}', method, params, callback, sessionId);
        }

    </script>

</head>

<body onload="init()">

    <div id="body" class="body">

        <h1 class="search" onclick="window.open('http://www.codewave.de')" style="cursor: pointer"><span><fmt:message key="myTunesRss" /></span></h1>

        <ul class="links">
            <li><a style="cursor:pointer" onclick="self.document.location.href='${mtfn:decode64(param.backUrl)}'">
                <fmt:message key="back" />
            </a></li>
            <li style="float:right"><a href="${servletUrl}/showPortal/${auth}">
                <fmt:message key="portal" />
            </a></li>
        </ul>

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
            <img src="${appUrl}/images/rc_shuffle.png" alt="shuffle" onclick="shuffle()" style="cursor:pointer"/>
            <img src="${appUrl}/images/rc_fullscreen.png" alt="fullscreen" onclick="toggleFullScreen()" style="cursor:pointer"/>
        </div>

        <div id="volumeDiv">
            <div id="volumeBackground" style="cursor:pointer">
                <div id="volumeBar" style="width:0;cursor:pointer">&nbsp;</div>
            </div>
        </div>

        <div id="progressDiv" style="display:block">
            <div id="progressBackground" style="cursor:pointer">
                <div id="progressBar" style="width:0;cursor:pointer">&nbsp;</div>
            </div>
        </div>

    </div>

</body>

</html>
