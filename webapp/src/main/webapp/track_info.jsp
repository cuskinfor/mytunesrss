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
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<%--@elvariable id="track" type="de.codewave.mytunesrss.datastore.statement.Track"--%>
<%--@elvariable id="tags" type="java.util.Collection"--%>
<%--@elvariable id="msgUnknown" type="java.lang.String"--%>
<%--@elvariable id="mp3info" type="java.lang.Boolean"--%>
<%--@elvariable id="avgBitRate" type="java.lang.Integer"--%>
<%--@elvariable id="avgSampleRate" type="java.lang.Integer"--%>
<%--@elvariable id="userAgent" type="java.lang.String"--%>

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
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal" /></a>
                <span><fmt:message key="myTunesRss" /></span>
            </h1>
        </div>
        
        <div class="content">
        
            <div class="content-inner">
    
                <jsp:include page="/incl_error.jsp" />
            
              <ul class="menu">
                <c:if test="${!empty param.backUrl}">
                  <li>
                    <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back" /></a>
                  </li>
                </c:if>
              </ul>
            
                <table cellspacing="0" class="settings">
                    <tr>
                        <th colspan="2" class="active">
                            <c:out value="${cwfn:choose(mtfn:unknown(track.originalArtist), msgUnknown, track.originalArtist)}" />
                            -
                            <c:out value="${track.name}" />
                        </th>
                    </tr>
                    <mt:initFlipFlop value1="" value2="class=\"odd\""/>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="track" />:
                        </td>
                        <td>
                            <c:out value="${track.name}" />
                        </td>
                    </tr>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="album" />:
                        </td>
                        <td>
                            <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" />
                        </td>
                    </tr>
                        <c:if test="${track.posNumber > 0}">
                            <tr <mt:flipFlop/>>
                                <td class="label">
                                    <fmt:message key="discnumber.label" />:
                                </td>
                                <td>
                                    <c:set var="msg"><fmt:message key="${cwfn:choose(track.posSize == 0, 'discnumber.numberonly', 'discnumber.numberofsize')}"/></c:set>
                                    <mt:array var="params">
                                        <mt:arrayElement value="${track.posNumber}"/>
                                        <mt:arrayElement value="${track.posSize}"/>
                                    </mt:array>
                                    <c:out value="${cwfn:choose(track.posSize == 0, track.posNumber, cwfn:message(msg, params))}" />
                                </td>
                            </tr>
                        </c:if>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="artist" />:
                        </td>
                        <td>
                            <c:out value="${cwfn:choose(mtfn:unknown(track.originalArtist), msgUnknown, track.originalArtist)}" />
                        </td>
                    </tr>
                    <tr <mt:flipFlop/>>
                        <td class="label">
                            <fmt:message key="duration" />:
                        </td>
                        <td>
                            <c:out value="${mtfn:duration(track)}" />
                        </td>
                    </tr>
                    <c:if test="${mp3info}">
                        <fmt:message var="localizedUnknown" key="unknown" />
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="bitrate" />:
                            </td>
                            <td>
                                <c:out value="${avgBitRate}" default="${localizedUnknown}"/>
                            </td>
                        </tr>
                        <tr <mt:flipFlop/>>
                            <td class="label">
                                <fmt:message key="samplerate" />:
                            </td>
                            <td>
                                <c:out value="${avgSampleRate}" default="${localizedUnknown}"/>
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
                            <c:if test="${track.protected}"><img src="${appUrl}/images/protected.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle" /></c:if>
                            <c:if test="${track.mediaType.jspName == 'Video'}"><img src="${appUrl}/images/movie.gif" alt="<fmt:message key="video"/>" style="vertical-align:middle" /></c:if>
                            <c:out value="${mtfn:suffix(null, null, track)}" />
                        </td>
                    </tr>
                    <tr <mt:flipFlop/>>
                        <td>&nbsp;</td>
                        <td class="actions">
                            <c:if test="${authUser.remoteControl && config.remoteControl && globalConfig.remoteControl}">
                                <a href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}" title="<fmt:message key="tooltip.remotecontrol"/>">Remote Control</a>
                            </c:if>
                            <c:if test="${authUser.rss && config.showRss}">
                                    <a class="rss" href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.xml" title="<fmt:message key="tooltip.rssfeed"/>">RSS</a>
                            </c:if>
                            <c:if test="${authUser.playlist && config.showPlaylist}">
                                    <a class="playlist" href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.${config.playlistFileSuffix}" title="<fmt:message key="tooltip.playlist"/>">Playlist</a>
                            </c:if>
                            <c:if test="${authUser.player && config.showPlayer}">
                                    <a class="flash" style="cursor:pointer" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=track=${track.id}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:virtualTrackName(track)}.xspf</mt:encrypt>'); return false;" title="<fmt:message key="tooltip.flashplayer"/>">Flash Player</a>
                            </c:if>
                            <c:if test="${authUser.download && config.showDownload}">
                                    <a class="download" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>" title="${track.name}" title="<fmt:message key="tooltip.playtrack"/>">Download</a>
                            </c:if>
                        </td>
                    </tr>
                    <c:if test="${authUser.download && config.showDownload}">
                        <tr <mt:flipFlop/>>
                            <td>&nbsp;</td>
                            <td>
                                <a href="<c:out value="${mtfn:downloadLink(pageContext, track, 'notranscode=true')}"/>">
                                    <img src="${appUrl}/images/download_odd.gif" alt="<fmt:message key="tooltip.originalDownload"/>" title="<fmt:message key="tooltip.originalDownload"/>" />
                                    <fmt:message key="originalDownload"/>
                                </a>
                            </td>
                        </tr>
                    </c:if>
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
                    <c:if test="${!empty(track.imageHash) || (track.mediaType.jspName == 'Video' && userAgent != 'Psp')}">
                        <tr>
                            <th colspan="2" class="active">
                                <c:choose>
                                    <c:when test="${track.mediaType.jspName == 'Video'}">
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
                                <c:when test="${track.mediaType.jspName == 'Video'}">
                                    <c:set var="imgUrl" value="${appUrl}/images/movie_poster.png"/>
                                    <c:if test="${!empty(track.imageHash)}"><c:set var="imgUrl">${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=256</mt:encrypt></c:set></c:if>
                                    <embed src="${imgUrl}" href="<c:out value="${mtfn:playbackLink(pageContext, track, 'notranscode=true')}"/>" type="${mtfn:contentType(config, authUser, track)}" <c:if test="${userAgent == 'Iphone'}">target="myself"</c:if> scale="1"></embed>
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
            <div class="footer-inner"></div>
        </div>
    
    </div>

</body>

</html>