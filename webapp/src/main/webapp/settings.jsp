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

    <script type="text/javascript">
        function toggleDownload() {
            if (document.getElementById("downloadCheckbox").checked) {
                document.getElementById("yahooPlayerCheckbox").disabled = false;
            } else {
                document.getElementById("yahooPlayerCheckbox").disabled = true;
            }
        }
    </script>
</head>

<body>

<div class="body">

    <h1 class="settings">
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><fmt:message key="portal" /></a> <span><fmt:message key="myTunesRss" /></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <form action="${servletUrl}/saveSettings/${auth}" method="post" onsubmit="document.getElementById('yahooPlayerCheckbox').disabled=false;return true">
        <table cellspacing="0">
            <tr>
                <th class="active"><fmt:message key="settings.info" /></th>
                <th>&nbsp;</th>
            </tr>
            <mt:initFlipFlop value1="" value2="class=\"odd\""/>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.username" /></td>
                <td><c:out value="${authUser.name}"/></td>
            </tr>
            <c:if test="${!authUser.editLastFmAccount && !empty authUser.lastFmUsername}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.lastFmUsername" /></td>
                    <td><c:out value="${authUser.lastFmUsername}"/></td>
                </tr>
            </c:if>
            <c:if test="${authUser.quota}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.quota" /></td>
                    <td><c:out value="${mtfn:memory(authUser.bytesQuota)}"/> ${authUser.quotaType}</td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.quotaRemain" /></td>
                    <td><c:out value="${mtfn:memory(authUser.quotaRemaining)}"/></td>
                </tr>
            </c:if>
            <tr>
                <th class="active"><fmt:message key="settings" /></th>
                <th>&nbsp;</th>
            </tr>
            <mt:initFlipFlop value1="" value2="class=\"odd\""/>
            <c:if test="${authUser.changePassword}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.password" /></td>
                    <td>
                        <input type="password"
                               name="password1"
                               maxlength="30"
                               value="<c:out value="${param.password1}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.retypePassword" /></td>
                    <td>
                        <input type="password"
                               name="password2"
                               maxlength="30"
                               value="<c:out value="${param.password2}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.editLastFmAccount}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.lastFmUsername" /></td>
                    <td>
                        <input name="lastfmusername"
                               maxlength="30"
                               value="<c:out value="${authUser.lastFmUsername}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.lastFmPassword" /></td>
                    <td>
                        <input type="password"
                               name="lastfmpassword1"
                               maxlength="30"
                               value="<c:out value="${param.lastfmpassword1}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.retypeLastFmPassword" /></td>
                    <td>
                        <input type="password"
                               name="lastfmpassword2"
                               maxlength="30"
                               value="<c:out value="${param.lastfmpassword2}"/>"
                               style="width: 170px;" />
                    </td>
                </tr>
            </c:if>
            <c:if test="${!empty themes}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.theme" /></td>
                    <td>
                        <select name="theme">
                            <option value="" <c:if test="${empty config.theme}">selected="selected"</c:if>><fmt:message key="theme.default"/></option>
                            <c:forEach items="${themes}" var="theme">
                                <option value="<c:out value="${theme}"/>" <c:if test="${config.theme eq theme}">selected="selected"</c:if>><c:out value="${theme}"/></option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
            </c:if>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.itemsPerPage" /></td>
                <td><input type="text"
                           name="pageSize"
                           maxlength="3"
                           value="<c:out value="${cwfn:choose(config.pageSize > 0, config.pageSize, '')}"/>"
                           style="width: 50px;" /></td>
            </tr>
            <c:if test="${authUser.rss}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.rssFeedLimit" /></td>
                    <td>
                        <input type="text"
                               name="rssFeedLimit"
                               maxlength="3"
                               value="<c:out value="${cwfn:choose(config.rssFeedLimit > 0, config.rssFeedLimit, '')}"/>"
                               style="width: 50px;" />
                    </td>
                </tr>
            </c:if>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.randomPlaylistSize" /></td>
                <td>
                    <input type="text"
                           name="randomPlaylistSize"
                           maxlength="3"
                           value="<c:out value="${cwfn:choose(config.randomPlaylistSize > 0, config.randomPlaylistSize, '')}"/>"
                           style="width: 50px;" />
                </td>
            </tr>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.randomSource" /></td>
                <td>
                    <select name="randomSource">
                        <option value=""><fmt:message key="settings.randomSourceAll"/></option>
                        <c:forEach items="${playlists}" var="playlist">
                            <option value="${playlist.id}" <c:if test="${config.randomSource eq playlist.id}">selected="selected"</c:if>><c:out value="${playlist.name}"/></option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.lastUpdatedPlaylistSize" /></td>
                <td>
                    <input type="text"
                           name="lastUpdatedPlaylistSize"
                           maxlength="3"
                           value="<c:out value="${cwfn:choose(config.lastUpdatedPlaylistSize > 0, config.lastUpdatedPlaylistSize, '')}"/>"
                           style="width: 50px;" />
                </td>
            </tr>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.mostPlayedPlaylistSize" /></td>
                <td>
                    <input type="text"
                           name="mostPlayedPlaylistSize"
                           maxlength="3"
                           value="<c:out value="${cwfn:choose(config.mostPlayedPlaylistSize > 0, config.mostPlayedPlaylistSize, '')}"/>"
                           style="width: 50px;" />
                </td>
            </tr>
            <tr <mt:flipFlop/>>
                <td><fmt:message key="settings.browserStartIndex" /></td>
                <td>
                    <select name="browserStartIndex">
                        <option value="" <c:if test="${config.browserStartIndex == ''}">selected="selected"</c:if>><fmt:message key="alphabetPagerAll"/></option>
                        <option value="0" <c:if test="${config.browserStartIndex == '0'}">selected="selected"</c:if>>0 - 9</option>
                        <option value="1" <c:if test="${config.browserStartIndex == '1'}">selected="selected"</c:if>>A - C</option>
                    </select>
                </td>
            </tr>
            <c:if test="${authUser.rss}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.playlistTypes.rss" /></td>
                    <td>
                        <input type="checkbox" name="feedType" value="rss" <c:if test="${config.showRss}">checked="checked"</c:if> />
                        <img src="${appUrl}/images/rss_odd.gif" alt="RSS" style="vertical-align:text-top;" />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.playlist}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.playlistTypes.playlist" /></td>
                    <td>
                        <input type="checkbox" name="feedType" value="playlist" <c:if test="${config.showPlaylist}"> checked="checked"</c:if> />
                        <img src="${appUrl}/images/playlist_odd.gif" alt="playlist" style="vertical-align:text-top;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.playlistType" /></td>
                    <td>
                        <select name="playlistType">
                            <option value="M3u" <c:if test="${config.playlistType eq 'M3u'}">selected="selected"</c:if>>m3u</option>
                            <option value="Xspf" <c:if test="${config.playlistType eq 'Xspf'}">selected="selected"</c:if>>xspf</option>
                            <option value="QtPlugin" <c:if test="${config.playlistType eq 'QtPlugin'}">selected="selected"</c:if>>iPhone</option>
                        </select>
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.download}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.showDownload" /></td>
                    <td>
                        <input id="downloadCheckbox" type="checkbox" name="showDownload" value="true" <c:if test="${config.showDownload}">checked="checked"</c:if> onclick="toggleDownload()"/>
                        <img src="${appUrl}/images/download.gif" alt="playlist" style="vertical-align:text-top;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.showYahooMediaPlayer" /></td>
                    <td>
                        <input id="yahooPlayerCheckbox" type="checkbox" name="showYahooMediaPlayer" value="true" <c:if test="${config.yahooMediaPlayer}">checked="checked"</c:if> <c:if test="${!config.showDownload}">disabled="disabled"</c:if> />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.player}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.showPlayer" /></td>
                    <td>
                        <input type="checkbox" name="showPlayer" value="true" <c:if test="${config.showPlayer}">checked="checked"</c:if> />
                        <img src="${appUrl}/images/player.gif" alt="player" style="vertical-align:text-top;" />
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.flashplayerType" /></td>
                    <td>
                        <select name="flashplayerType">
                            <option value="jw" <c:if test="${config.flashplayerType eq 'jw'}">selected="selected"</c:if>><fmt:message key="flashplayer.jw"/></option>
                            <option value="xspf" <c:if test="${config.flashplayerType eq 'xspf'}">selected="selected"</c:if>><fmt:message key="flashplayer.xspf"/></option>
                        </select>
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.transcoder && globalConfig.validLameBinary}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.useLame" /></td>
                    <td>
                        <input type="checkbox" name="useLame" value="true" <c:if test="${config.lame}">checked="checked"</c:if> />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.transcoder && globalConfig.validLameBinary && globalConfig.validFaadBinary}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.useFaad" /></td>
                    <td>
                        <input type="checkbox" name="useFaad" value="true" <c:if test="${config.faad}">checked="checked"</c:if> />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.transcoder && globalConfig.validLameBinary && globalConfig.validAlacBinary}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.useAlac" /></td>
                    <td>
                        <input type="checkbox" name="useAlac" value="true" <c:if test="${config.alac}">checked="checked"</c:if> />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.transcoder && (globalConfig.validLameBinary || globalConfig.validFaadBinary)}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.transcodeOnTheFlyIfPossible" /></td>
                    <td>
                        <input type="checkbox" name="transcodeOnTheFlyIfPossible" value="true" <c:if test="${config.transcodeOnTheFlyIfPossible}">checked="checked"</c:if> />
                    </td>
                </tr>
            </c:if>
            <c:if test="${authUser.transcoder && (globalConfig.validLameBinary || globalConfig.validFaadBinary)}">
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.lameTargetBitrate" /></td>
                    <td>
                        <select name="lameTargetBitrate">
                            <option value="32" <c:if test="${config.lameTargetBitrate eq '32'}">selected="selected"</c:if>>32 kbit</option>
                            <option value="64" <c:if test="${config.lameTargetBitrate eq '64'}">selected="selected"</c:if>>64 kbit</option>
                            <option value="96" <c:if test="${config.lameTargetBitrate eq '96'}">selected="selected"</c:if>>96 kbit</option>
                            <option value="128" <c:if test="${config.lameTargetBitrate eq '128'}">selected="selected"</c:if>>128 kbit</option>
                            <option value="192" <c:if test="${config.lameTargetBitrate eq '192'}">selected="selected"</c:if>>192 kbit</option>
                        </select>
                    </td>
                </tr>
                <tr <mt:flipFlop/>>
                    <td><fmt:message key="settings.lameTargetSampleRate" /></td>
                    <td>
                        <select name="lameTargetSampleRate">
                            <option value="11025" <c:if test="${config.lameTargetSampleRate eq '11025'}">selected="selected"</c:if>>11025 Hz</option>
                            <option value="22050" <c:if test="${config.lameTargetSampleRate eq '22050'}">selected="selected"</c:if>>22050 Hz</option>
                            <option value="44100" <c:if test="${config.lameTargetSampleRate eq '44100'}">selected="selected"</c:if>>44100 Hz</option>
                        </select>
                    </td>
                </tr>
            </c:if>
        </table>
        <div class="buttons">
            <input type="submit" value="<fmt:message key="doSave"/>" />
            <input type="button" value="<fmt:message key="doCancel"/>" onclick="document.location.href='${servletUrl}/showPortal/${auth}'" />
        </div>
    </form>
</div>

</body>

</html>
