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
    <script src="${appUrl}/js/jquery-ui.js?ts=${sessionCreationTime}" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="${themeUrl}/styles/jquery-ui/jquery-ui.css" />

    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, maximum-scale=1.0" />

    <script type="text/javascript">

        // make JSON arrays work in REST client framework
        JSON = JSON || {};
        JSON.stringify = function(value) { return value.toJSON(); };
        JSON.parse = JSON.parse || function(jsonsring) { return jsonsring.evalJSON(true); };
        
        var playlistVersion = ${playlistVersion};

        var trackNames = new Array(
            <c:forEach items="${tracks}" var="track" varStatus="trackLoopStatus">
                "<c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknownArtist, track.artist)}" /> - <c:out value="${cwfn:choose(mtfn:unknown(track.name), msgUnknownTrack, track.name)}" />"<c:if test="${!trackLoopStatus.last}">,</c:if>
            </c:forEach>
        );

        var imageUrls = new Array(
                <c:forEach items="${tracks}" var="track" varStatus="trackLoopStatus">
                    "<c:if test="${!empty(track.imageHash)}">${servletUrl}/showImage/${auth}/<mt:encrypt>hash=${track.imageHash}/size=32</mt:encrypt></c:if>"<c:if test="${!trackLoopStatus.last}">,</c:if>
                </c:forEach>
        );

        var itemsPerPage = ${itemsPerPage};
        var pagesPerPager = ${pagesPerPager};
        var currentPage = ${currentPage};
        var myFullScreen = false;

        function getFirstPage(){
            return (Math.floor(currentPage / pagesPerPager) * pagesPerPager);
        }

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
                    document.getElementById("linkStart" + i).innerHTML = trackNames[start + i];
                    if (imageUrls[start + i] != "") {
                        document.getElementById("cover" + i).innerHTML = "<img src=\"" + imageUrls[start + i] + "\"/>";
                    } else {
                        document.getElementById("cover" + i).innerHTML = "&nbsp;";
                    }
                } else {
                    document.getElementById("trackrow" + i).style.display = "none";
                }
            }
            if (currentPage >= pagesPerPager) {
                document.getElementById("pager_first").style.display = "inline-block";
                document.getElementById("pager_previous").style.display = "inline-block";
            } else {
                document.getElementById("pager_first").style.display = "none";
                document.getElementById("pager_previous").style.display = "none";
            }
            if ((currentPage + 1) * itemsPerPage < trackNames.length) {
                document.getElementById("pager_next").style.display = "inline-block";
                document.getElementById("pager_last").style.display = "inline-block";
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
                            document.getElementById("pager_active_" + i).innerHTML = (start + i + 1);
                            document.getElementById("pager_active_" + i).style.display = "inline-block";
                            document.getElementById("pager_inactive_" + i).style.display = "none";
                        } else {
                            document.getElementById("pager_inactive_" + i).innerHTML = (start + i + 1);
                            document.getElementById("pager_active_" + i).style.display = "none";
                            document.getElementById("pager_inactive_" + i).style.display = "inline-block";
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
            updateInterface(MediaPlayerResource.setStatus({action:"PLAY",track:"" + (currentPage * itemsPerPage + index)}));
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
                $jQ("#rc_play").css("display", "none");
                $jQ("#rc_pause").css("display", "inline");
            } else if (!trackInfo.playing) {
                $jQ("#rc_play").css("display", "inline");
                $jQ("#rc_pause").css("display", "none");
            }

            var percentage = trackInfo.currentTime != -1 && trackInfo.length > -1 ? trackInfo.currentTime * 100 / trackInfo.length : 0;
            if (percentage > 0 && percentage <= 100) {
                $jQ("#progress").slider("value", percentage);
            } else {
                $jQ("#progress").slider("value", 0);
            }

            if (trackInfo.volume > 0 && trackInfo.volume <= 100) {
                $jQ("#volume").slider("value", trackInfo.volume);
            } else {
                $jQ("#volume").slider("value", 0);
            }

            if (playlistVersion != trackInfo.playlistVersion) {
                self.document.location.href = '${servletUrl}/showRemoteControl/${auth}/backUrl=${param.backUrl}/fullScreen=' + myFullScreen;
            }
        }

        function play() {
            updateInterface(MediaPlayerResource.setStatus({action:"PLAY"}));
        }

        function pause() {
            updateInterface(MediaPlayerResource.setStatus({action:"PAUSE"}));
        }

        function stop() {
            updateInterface(MediaPlayerResource.setStatus({action:"STOP"}));
        }

        function nextTrack() {
            updateInterface(MediaPlayerResource.setStatus({action:"NEXT"}));
        }

        function previousTrack() {
            updateInterface(MediaPlayerResource.setStatus({action:"PREVIOUS"}));
        }

        function shuffle() {
            self.document.location.href = '${servletUrl}/showRemoteControl/${auth}/shuffle=true/backUrl=${param.backUrl}/fullScreen=' + myFullScreen;
        }

        function toggleFullScreen() {
            var newStatus = MediaPlayerResource.setStatus({fullscreen:!myFullScreen});
            myFullScreen = !myFullScreen;
            updateInterface(newStatus);
        }

        function init() {
            createPlaylist();
            init2(MediaPlayerResource.getStatus());
        }

        function getStateAndUpdateInterfacePeriodic() {
            updateInterface(MediaPlayerResource.getStatus());
            setTimeout(getStateAndUpdateInterfacePeriodic, 2000);
        }

        function init2(trackInfo) {
            getStateAndUpdateInterfacePeriodic();
            <c:choose>
                <c:when test="${empty currentlySelectedMediaRenderer }">
                    openMediaRendererSelection();
                </c:when>
                <c:otherwise>
                    if (!trackInfo.playing) {
                        <c:choose>
                            <c:when test="${param.fullScreen == 'true'}">
                                startPlayback(0);
                                toggleFullScreen();
                            </c:when>
                            <c:otherwise>
                                startPlayback(0);
                            </c:otherwise>
                        </c:choose>
                    }
                </c:otherwise>
            </c:choose>
        }

      $jQ(document).ready(function(){
        $jQ("#volume").slider({
            value:0,
            slide:function(event, ui) {
                updateInterface(MediaPlayerResource.setStatus({volume:ui.value}));
            }
        });
        $jQ("#progress").slider({
            value:0,
            slide:function(event, ui) {
                updateInterface(MediaPlayerResource.setStatus({action:"SEEK",seek:ui.value}));
            }
        });
      });

    </script>

</head>

<body class="remote" onload="init()">

    <div id="body" class="body">

    	<div class="head">
	        <h1>
		        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal" /></span></a>
		        <span><fmt:message key="myTunesRss" /></span>
	        </h1>
	    </div>

	    <div class="content">

	    	<div class="content-inner">

		        <ul class="menu">
                    <li class="settings first"><a id="linkClear" href="${servletUrl}/clearRemotePlaylist/${auth}">
                        <fmt:message key="doClearRemotePlaylist" />
                    </a></li>
                    <li class="settings"><a id="linkSpeaker" style="cursor:pointer" onclick="openMediaRendererSelection()">
                        <fmt:message key="selectMediaRenderer" />
                    </a></li>
                    <li class="spacer">&nbsp;</li>
		            <li class="back"><a id="linkBack" style="cursor:pointer" onclick="self.document.location.href='${mtfn:decode64(param.backUrl)}'">
		                <fmt:message key="back" />
		            </a></li>
		        </ul>

                <div class="mediarenderertop"></div>
                <div class="mediarenderer">
            		<span><strong><fmt:message key="selectedMediaRenderer"/>:</strong></span>&nbsp;<span id="mediaRendererName">${currentlySelectedMediaRenderer}</span>
                </div>
                <div class="mediarendererbottom"></div>

		        <div class="navigation">

			        <div class="remotecontrolPanel">
			            <img id="linkPrevious" src="${themeUrl}/images/rc_prev.png" alt="prev" onclick="previousTrack()" style="cursor:pointer"/>
			            <img id="rc_play" src="${themeUrl}/images/rc_play.png" alt="prev" onclick="play()" style="cursor:pointer;display:none"/>
			            <img id="rc_pause" src="${themeUrl}/images/rc_pause.png" alt="prev" onclick="pause()" style="cursor:pointer"/>
			            <img id="linkStop" src="${themeUrl}/images/rc_stop.png" alt="stop" onclick="stop()" style="cursor:pointer"/>
			            <img id="linkNext" src="${themeUrl}/images/rc_next.png" alt="next" onclick="nextTrack()" style="cursor:pointer"/>
			            <img id="linkShuffle" src="${themeUrl}/images/rc_shuffle.png" alt="shuffle" onclick="shuffle()" style="cursor:pointer"/>
			            <img id="linkFullscreen" src="${themeUrl}/images/rc_fullscreen.png" alt="fullscreen" onclick="toggleFullScreen()" style="cursor:pointer"/>
			        </div>

			        <div class="volumeContainer">
				        Volume<br />
				        <div id="volume"></div>
					</div>
					<div class="progressContainer">
						Progress<br />
						<div id="progress"></div>
	        		</div>

		        </div>

		        <table cellspacing="0" class="tracklist">

		            <c:forEach begin="0" end="9" varStatus="itemLoopStatus">
		                <tr id="trackrow${itemLoopStatus.index}" class="${cwfn:choose(itemLoopStatus.count % 2 == 0, 'even', 'odd')}">
		                    <td id="cover${itemLoopStatus.index}" class="remotecontrolTrackImage">&nbsp;</td>
		                    <td id="linkStart${itemLoopStatus.index}" style="cursor:pointer" onclick="startPlayback(${itemLoopStatus.index})" class="artist"></td>
		                </tr>
		            </c:forEach>

		        </table>

                <div id="pager" class="pager">

		            <a id="pager_first" onclick="currentPage = 0;createPlaylist()" class="first">First</a>
		            <a id="pager_previous" onclick="currentPage = getFirstPage() - 1;createPlaylist()" class="previous">Previous</a>

		            <c:forEach begin="0" end="9" varStatus="status">
		                <a id="pager_active_${status.index}" class="active">&nbsp;</a>
		                <a id="pager_inactive_${status.index}" style="cursor:pointer" onclick="currentPage = getFirstPage() + ${status.index};createPlaylist()">&nbsp;</a>
		            </c:forEach>

		            <a id="pager_next" onclick="currentPage = getFirstPage() + pagesPerPager;createPlaylist()" class="next">Next</a>
		            <a id="pager_last" onclick="currentPage = Math.floor(trackNames.length / itemsPerPage);createPlaylist()" class="last">Last</a>

		        </div>

			</div>

		</div>

		<div class="footer">
			<div class="inner"></div>
		</div>

	</div>

    <script type="text/javascript">
        function selectMediaRenderer(rendererId, rendererName) {
            $jQ.modal.close();
            showLoading('<fmt:message key="switchingMediaRenderer"/>');
            MediaPlayerResource.setStatus({renderer:rendererId});
            $jQ("#mediaRendererName").empty();
            $jQ("#mediaRendererName").append(rendererName.substr(0, 30));
            hideLoading();
        }

        function openMediaRendererSelection() {
            var mediaRenderers = SessionResource.getSession().mediaRenderers;
            if (mediaRenderers.length > 0) {
                $jQ("#devicelist").empty();
                for (var i = 0; i < mediaRenderers.length; i++) {
                    var mediaRendererName = mediaRenderers[i].name.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
                    $jQ("#devicelist").append("<a onclick='selectMediaRenderer(\"" + mediaRenderers[i].id + "\", \"" + mediaRendererName.replace(/"/g, "\"") + "\")'>" + mediaRendererName + "</a><br/>");
                }
                openDialog("#mediaRendererSelectionDialog");
            }
        }
    </script>

    <div id="mediaRendererSelectionDialog" class="dialog">
        <h2>
            <fmt:message key="mediaRendererSelectionDialog.title" />
        </h2>

        <div>
            <p>
                <fmt:message key="mediaRendererSelectionDialog.text" />
            </p>

            <p class="mediarendererdevicelist" id="devicelist"></p>

            <p align="right">
                <button id="mediaRendererSelectionDialogCancel" onclick="$jQ.modal.close()"><fmt:message key="doCancel"/></button>
            </p>
        </div>

    </div>

</body>

</html>
