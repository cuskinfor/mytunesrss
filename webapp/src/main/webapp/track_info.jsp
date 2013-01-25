<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="originalDownloadLink" type="java.lang.Boolean"--%>
<%--@elvariable id="track" type="de.codewave.mytunesrss.datastore.statement.Track"--%>
<%--@elvariable id="tags" type="java.util.Collection"--%>
<%--@elvariable id="mp3info" type="de.codewave.camel.mp3.Mp3Info"--%>
<%--@elvariable id="userAgent" type="java.lang.String"--%>
<%--@elvariable id="msgUnknownSeries" type="java.lang.String"--%>
<%--@elvariable id="msgUnknownAlbum" type="java.lang.String"--%>
<%--@elvariable id="msgUnknownArtist" type="java.lang.String"--%>
<%--@elvariable id="themeUrl" type="java.lang.String"--%>

<c:set var="backUrl" scope="request">${servletUrl}/showTrackInfo/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(param.track)}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="trackinfo">

    <div class="body">

        <div class="head">
            <h1>
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal" /></span></a>
                <span><fmt:message key="myTunesRss" /></span>
            </h1>
        </div>

        <div class="content">

            <div class="content-inner">

                <jsp:include page="/incl_error.jsp" />

              <ul class="menu">
                <c:if test="${!empty param.backUrl}">
                  <li class="back">
                    <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back" /></a>
                  </li>
                </c:if>
              </ul>

                <table cellspacing="0" class="settings">
                    <tr>
                        <th colspan="2" class="active">
                            <c:out value="${cwfn:choose(mtfn:unknown(track.originalArtist), msgUnknownArtist, track.originalArtist)}" />
                            -
                            <c:out value="${track.name}" />
                        </th>
                    </tr>
                    <mt:initFlipFlop value1="" value2="class=\"odd\""/>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="title" />:
                        </td>
                        <td>
                            <c:out value="${track.name}" />
                        </td>
                    </tr>
                    <c:choose>
                        <c:when test="${track.mediaType == 'Audio'}">
                            <tr <mt:flipFlop/>>
                                <td class="label">
                                    <fmt:message key="album"/>:
                                </td>
                                <td>
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknownAlbum, track.album)}"/>
                                </td>
                            </tr>
                            <c:if test="${track.posNumber > 0}">
                                <tr <mt:flipFlop/>>
                                    <td class="label">
                                        <fmt:message key="discnumber.label"/>:
                                    </td>
                                    <td>
                                        <c:set var="msg"><fmt:message
                                                key="${cwfn:choose(track.posSize == 0, 'discnumber.numberonly', 'discnumber.numberofsize')}"/></c:set>
                                        <mt:array var="params">
                                            <mt:arrayElement value="${track.posNumber}"/>
                                            <mt:arrayElement value="${track.posSize}"/>
                                        </mt:array>
                                        <c:out value="${cwfn:choose(track.posSize == 0, track.posNumber, cwfn:message(msg, params))}"/>
                                    </td>
                                </tr>
                            </c:if>
                            <tr <mt:flipFlop/>>
                                <td class="label">
                                    <fmt:message key="artist"/>:
                                </td>
                                <td>
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.originalArtist), msgUnknownArtist, track.originalArtist)}"/>
                                </td>
                            </tr>
                            <c:if test="${!empty track.composer}">
                                <tr <mt:flipFlop/>>
                                    <td class="label">
                                        <fmt:message key="composer"/>:
                                    </td>
                                    <td><c:out value="${track.composer}"/></td>
                                </tr>
                            </c:if>
                        </c:when>
                        <c:when test="${track.mediaType == 'Video' && track.videoType == 'TvShow'}">
                            <tr <mt:flipFlop/>>
                                <td class="label">
                                    <fmt:message key="series"/>:
                                </td>
                                <td>
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.series), msgUnknownSeries, track.series)}"/>
                                </td>
                            </tr>
                            <tr <mt:flipFlop/>>
                                <td class="label">
                                    <fmt:message key="season"/>:
                                </td>
                                <td>
                                    ${track.season}
                                </td>
                            </tr>
                            <tr <mt:flipFlop/>>
                                <td class="label">
                                    <fmt:message key="episode"/>:
                                </td>
                                <td>
                                    ${track.episode}
                                </td>
                            </tr>
                        </c:when>
                    </c:choose>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="duration" />:
                        </td>
                        <td>
                            <c:out value="${mtfn:duration(track)}" />
                        </td>
                    </tr>
                    <c:if test="${!empty mp3info}">
                        <fmt:message var="localizedUnknown" key="unknown" />
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="bitrate" />:
                            </td>
                            <td>
                                <c:out value="${mp3info.avgBitrate}" default="${localizedUnknown}"/>
                                <c:if test="${mp3info.vbr}">(VBR)</c:if>
                            </td>
                        </tr>
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="samplerate" />:
                            </td>
                            <td>
                                <c:out value="${mp3info.avgSampleRate}" default="${localizedUnknown}"/>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${!empty track.comment}">
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="trackComment" />:
                            </td>
                            <td>
                                <c:forEach var="comment" varStatus="loopStatus" items="${mtfn:splitComments(track.comment)}">
                                    <c:out value="${comment}"/>
                                    <c:if test="${!loopStatus.last}"><br /></c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${!empty tags}">
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="trackTags" />:
                            </td>
                            <td>
                                <c:forEach var="tag" items="${tags}">
                                    <c:out value="${tag}"/>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:if>
                    <tr <mt:flipFlop/>>
                        <td class="label"><fmt:message key="type"/>:</td>
                        <td>
                            <c:if test="${track.protected}"><img src="${themeUrl}/images/protected.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle" /></c:if>
                            <c:if test="${track.mediaType == 'Video'}"><img src="${themeUrl}/images/${cwfn:choose(track.videoType == 'Movie', 'movie.png', 'tvshow.png')}" alt="<fmt:message key="video"/>" style="vertical-align:middle" /></c:if>
                            <c:out value="${mtfn:suffix(null, null, track)}" />
                        </td>
                    </tr>
                    <tr <mt:flipFlop/>>
                        <td>&nbsp;</td>
                        <td class="actions">
                            <c:if test="${authUser.remoteControl && config.remoteControl && globalConfig.remoteControl}">
                                <a id="linkRemoteControl" class="remote" href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}" title="<fmt:message key="tooltip.remotecontrol"/>"><span>Remote Control</span></a>
                            </c:if>
                            <c:if test="${authUser.rss && config.showRss}">
                                    <a id="linkRssFeed" class="rss" href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/_cdi=${cwfn:encodeUrl(mtfn:virtualTrackName(track))}.xml</mt:encrypt>" title="<fmt:message key="tooltip.rssfeed"/>"><span>RSS</span></a>
                            </c:if>
                            <c:if test="${authUser.playlist && config.showPlaylist}">
                                    <a id="linkPlaylist" class="playlist" href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/type=${config.playlistType}/_cdi=${cwfn:encodeUrl(mtfn:virtualTrackName(track))}.${config.playlistFileSuffix}</mt:encrypt>" title="<fmt:message key="tooltip.playlist"/>"><span>Playlist</span></a>
                            </c:if>
                            <c:if test="${globalConfig.flashPlayer && authUser.player && config.showPlayer}">
                                <c:set var="playlistParams"><mt:encode64>track=${track.id}</mt:encode64></c:set>
                                    <a id="linkFlashPlayer" class="flash" style="cursor:pointer" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=${playlistParams}/playerId='); return false;" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                    <a id="linkDownload" class="download" href="<c:out value="${mtfn:downloadLink(pageContext, track, null)}"/>" title="<fmt:message key="tooltip.playtrack"/>"><span>Download</span></a>
                            </c:if>
                        </td>
                    </tr>
                    <c:if test="${originalDownloadLink && authUser.download && config.showDownload}">
                        <tr <mt:flipFlop/>>
                            <td>&nbsp;</td>
                            <td>
                                <a id="linkDownloadOriginal" href="<c:out value="${mtfn:downloadLink(pageContext, track, 'notranscode=true')}"/>" class="original" title="<fmt:message key="tooltip.originalDownload"/>">
                                    <fmt:message key="originalDownload"/>
                                </a>
                            </td>
                        </tr>
                    </c:if>
                    <tr class="spacerRow">
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <th colspan="2" class="active">
                            <fmt:message key="trackinfo.statistics"/>
                        </th>
                    </tr>
                    <mt:initFlipFlop value1="" value2="class=\"odd\""/>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="trackinfo.playcount" />:
                        </td>
                        <td>
                            <c:out value="${track.playCount}" />
                        </td>
                    </tr>
                    <c:if test="${track.tsPlayed > 0}">
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="trackinfo.lastPlayed" />:
                            </td>
                            <td>
                                <c:out value="${mtfn:dateTime(pageContext.request, track.tsPlayed)}" />
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${track.tsUpdated > 0}">
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="trackinfo.lastUpdate" />:
                            </td>
                            <td>
                                <c:out value="${mtfn:dateTime(pageContext.request, track.tsUpdated)}" />
                            </td>
                        </tr>
                    </c:if>
                    <c:if test="${!empty(track.imageHash) || (track.mediaType == 'Video')}">
                        <tr>
                            <th colspan="2" class="active">
                                <c:choose>
                                    <c:when test="${track.mediaType == 'Video'}">
                                        <fmt:message key="trackinfo.movie"/>
                                    </c:when>
                                    <c:otherwise>
                                        <fmt:message key="trackinfo.cover"/>
                                    </c:otherwise>
                                </c:choose>
                            </th>
                        </tr>
                        <tr>
                          <td colspan="2" align="center">
                            <c:choose>
                                <c:when test="${track.mediaType == 'Video'}">
                                    <c:set var="imgUrl" value="${themeUrl}/images/movie_poster.png"/>
                                    <c:if test="${!empty(track.imageHash)}"><c:set var="imgUrl">${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=256</mt:encrypt></c:set></c:if>
                                    <embed src="${imgUrl}" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>" type="${mtfn:contentType(config, authUser, track)}" <c:if test="${userAgent == 'Iphone'}">target="myself"</c:if> scale="1"></embed>
                                </c:when>
                                <c:otherwise>
                                    <img alt="${track.name} Album Art"
                                      src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=256</mt:encrypt>"
                                      width="200" style="display: block; margin: 10px auto;"/>
                                </c:otherwise>
                            </c:choose>
                          </td>
                        </tr>
                    </c:if>
                    </table>

            </div>

        </div>

        <div class="footer">
            <div class="inner"></div>
        </div>

    </div>

    <jsp:include page="incl_select_flashplayer_dialog.jsp"/>

</body>

</html>