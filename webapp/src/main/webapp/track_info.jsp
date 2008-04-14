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

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

<div class="body">

    <h1 class="info">
        <a class="portal" href="${servletUrl}/showPortal/${auth}">
            <fmt:message key="portal" />
        </a> <span><fmt:message key="myTunesRss" /></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

  <ul class="links">
    <c:if test="${!empty param.backUrl}">
      <li>
        <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back" /></a>
      </li>
    </c:if>
  </ul>

    <table cellspacing="0">
        <tr>
            <th colspan="2" class="active">
                <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
                -
                <c:out value="${track.name}" />
            </th>
        </tr>
        <mt:initFlipFlop value1="" value2="class=\"odd\""/>
        <tr <mt:flipFlop/>>
            <td style="white-space:nowrap">
                <fmt:message key="track" />:
            </td>
            <td>
                <c:out value="${track.name}" />
            </td>
        </tr>
        <tr <mt:flipFlop/>>
            <td style="white-space:nowrap">
                <fmt:message key="album" />:
            </td>
            <td>
                <c:out value="${cwfn:choose(mtfn:unknown(track.album), msgUnknown, track.album)}" />
            </td>
        </tr>
            <c:if test="${track.posNumber > 0}">
                <tr <mt:flipFlop/>>
                    <td style="white-space:nowrap">
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
            <td style="white-space:nowrap">
                <fmt:message key="artist" />:
            </td>
            <td>
                <c:out value="${cwfn:choose(mtfn:unknown(track.artist), msgUnknown, track.artist)}" />
            </td>
        </tr>
        <tr <mt:flipFlop/>>
            <td style="white-space:nowrap">
                <fmt:message key="duration" />:
            </td>
            <td>
                <c:out value="${mtfn:duration(track)}" />
            </td>
        </tr>
        <c:if test="${mp3info}">
            <fmt:message var="localizedUnknown" key="unknown" />
            <tr <mt:flipFlop/>>
                <td style="white-space:nowrap">
                    <fmt:message key="bitrate" />:
                </td>
                <td>
                    <c:out value="${mtfn:bitrate(track)}" default="${localizedUnknown}"/>
                </td>
            </tr>
            <tr <mt:flipFlop/>>
                <td style="white-space:nowrap">
                    <fmt:message key="samplerate" />:
                </td>
                <td>
                    <c:out value="${mtfn:samplerate(track)}" default="${localizedUnknown}"/>
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty track.comment}">
            <tr <mt:flipFlop/>>
                <td style="white-space:nowrap;vertical-align:top">
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
        <tr <mt:flipFlop/>>
            <td style="white-space:nowrap"><fmt:message key="type"/>:</td>
            <td>
                <c:if test="${track.protected}"><img src="${appUrl}/images/protected.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle" /></c:if>
                <c:if test="${track.video}"><img src="${appUrl}/images/movie.gif" alt="<fmt:message key="video"/>" style="vertical-align:middle" /></c:if>
                <c:out value="${mtfn:suffix(null, null, track)}" />
            </td>
        </tr>
        <tr <mt:flipFlop/>>
            <td>&nbsp;</td>
            <td>
                        <c:if test="${authUser.rss && config.showRss}">
                                <a href="${permServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.xml">
                                    <img src="${appUrl}/images/rss${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
                        </c:if>
                        <c:if test="${authUser.playlist && config.showPlaylist}">
                                <a href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.${config.playlistFileSuffix}">
                                    <img src="${appUrl}/images/playlist${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
                        </c:if>
                        <c:if test="${authUser.player && config.showPlayer}">
                                <a href="#" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=track=${track.id}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${mtfn:virtualTrackName(track)}.xspf</mt:encrypt>'); return false">
                                    <img src="${appUrl}/images/player${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
                        </c:if>
                        <c:if test="${authUser.download && config.showDownload}">
                                <a href="${servletUrl}/downloadTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}</mt:encrypt>/${mtfn:virtualTrackName(track)}.${mtfn:suffix(config, authUser, track)}" title="${track.name}">
                                    <img src="${appUrl}/images/download${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playtrack"/>" title="<fmt:message key="tooltip.playtrack"/>" />
                                </a>
                        </c:if>
            </td>
        </tr>
        <c:if test="${authUser.download && config.showDownload}">
            <tr <mt:flipFlop/>>
                <td>
                    &nbsp;
                </td>
                <td>
                    <a href="${servletUrl}/playTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/notranscode=true</mt:encrypt>/${mtfn:virtualTrackName(track)}.${mtfn:suffix(null, null, track)}">
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
            <td style="white-space:nowrap">
                <fmt:message key="trackinfo.playcount" />:
            </td>
            <td>
                <c:out value="${track.playCount}" />
            </td>
        </tr>
        <c:if test="${track.tsPlayed > 0}">
            <tr <mt:flipFlop/>>
                <td style="white-space:nowrap">
                    <fmt:message key="trackinfo.lastPlayed" />:
                </td>
                <td>
                    <c:out value="${mtfn:dateTime(pageContext.request, track.tsPlayed)}" />
                </td>
            </tr>
        </c:if>
        <c:if test="${track.tsUpdated > 0}">
            <tr <mt:flipFlop/>>
                <td style="white-space:nowrap">
                    <fmt:message key="trackinfo.lastUpdate" />:
                </td>
                <td>
                    <c:out value="${mtfn:dateTime(pageContext.request, track.tsUpdated)}" />
                </td>
            </tr>
        </c:if>
        <c:if test="${track.imageCount > 0}">
            <tr>
                <th colspan="2" class="active">
                    <fmt:message key="trackinfo.cover"/>
                </th>
            </tr>
            <tr>
              <td colspan="2">
                <img alt="${track.name} Album Art"
                  src="${servletUrl}/showTrackImage/${auth}/<mt:encrypt key="${encryptionKey}">track=${track.id}/size=256</mt:encrypt>"
                  width="200" style="display: block; margin: 10px auto;"/>
              </td>
            </tr>
        </c:if>
        </table>

</div>

</body>

</html>