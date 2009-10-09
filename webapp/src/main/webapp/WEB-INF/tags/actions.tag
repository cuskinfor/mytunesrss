<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%@ attribute name="index" required="true" type="java.lang.Integer" %>
<%@ attribute name="backUrl" required="true" %>
<%@ attribute name="linkFragment" required="true" %>
<%@ attribute name="filename" required="true" %>
<%@ attribute name="zipFileCount" required="false" type="java.lang.Integer" %>
<%@ attribute name="track" required="false" type="de.codewave.mytunesrss.datastore.statement.Track" %>

<%@ variable scope="AT_END" name-given="yahoo" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<c:if test="${authUser.remoteControl && globalConfig.remoteControl}">
    <a id="fn_remotecontrol${index}" href="${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/backUrl=${backUrl}" style="display:${cwfn:choose(config.remoteControl, "inline", "none")}">
        <img src="${appUrl}/images/remote_control${cwfn:choose(index % 2 == 0, '', '_odd')}.gif"
             alt="<fmt:message key="tooltip.remotecontrol"/>" title="<fmt:message key="tooltip.remotecontrol"/>" /> </a>
</c:if>
<c:if test="${authUser.rss}">
    <a id="fn_rss${index}" href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/${filename}.xml" style="display:${cwfn:choose(config.showRss, "inline", "none")}">
        <img src="${appUrl}/images/rss${cwfn:choose(index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.rssfeed"/>" title="<fmt:message key="tooltip.rssfeed"/>" /> </a>
</c:if>
<c:if test="${authUser.playlist}">
    <a id="fn_playlist${index}" href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/${filename}.${config.playlistFileSuffix}" style="display:${cwfn:choose(config.showPlaylist, "inline", "none")}">
        <img src="${appUrl}/images/playlist${cwfn:choose(index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playlist"/>" title="<fmt:message key="tooltip.playlist"/>" /> </a>
</c:if>
<c:if test="${authUser.player}">
    <a id="fn_player${index}" style="cursor:pointer;display:${cwfn:choose(config.showPlayer, "inline", "none")}" onclick="openPlayer('${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=${linkFragment}</mt:encrypt>/<mt:encrypt key="${encryptionKey}">filename=${filename}.xspf</mt:encrypt>'); return false;">
        <img src="${appUrl}/images/player${cwfn:choose(index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.flashplayer"/>" title="<fmt:message key="tooltip.flashplayer"/>" /> </a>
</c:if>
<c:if test="${authUser.download}">
    <c:choose>
        <c:when test="${!empty track}">
            <c:choose>
                <c:when test="${!config.yahooMediaPlayer || mtfn:lowerSuffix(config, authUser, track) ne 'mp3'}">
                    <a id="fn_download${index}" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>" style="display:${cwfn:choose(config.showDownload, "inline", "none")}">
                        <img src="${appUrl}/images/download${cwfn:choose(index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.playtrack"/>" title="<fmt:message key="tooltip.playtrack"/>" />
                    </a>
                </c:when>
                <c:otherwise>
                    <c:set var="yahoo" value="true"/>
                    <a class="htrack" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>" title="<c:out value="${track.name}"/>">
                        <img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=128</mt:encrypt>" style="display:none" alt=""/>
                    </a>
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:when test="${authUser.maximumZipEntries <= 0 || zipFileCount <= authUser.maximumZipEntries}">
            <a id="fn_download${index}" href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/${filename}.zip" style="display:${cwfn:choose(config.showDownload, "inline", "none")}">
                <img src="${appUrl}/images/download${cwfn:choose(index % 2 == 0, '', '_odd')}.gif"
                     alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
        </c:when>
        <c:otherwise>
            <a id="fn_download${index}" style="cursor:pointer;display:${cwfn:choose(config.showDownload, "inline", "none")}" onclick="alert('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>'); return false;">
                <img src="${appUrl}/images/download${cwfn:choose(index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="tooltip.downloadzip"/>" title="<fmt:message key="tooltip.downloadzip"/>" /></a>
        </c:otherwise>
    </c:choose>
</c:if>
<a style="cursor:pointer" onclick="openFunctionsMenu(${index}, $jQ('#functionsDialogName${index}').text())">
    <img src="${appUrl}/images/menu.png"
         alt="TODO: functions menu" title="TODO: functions menu" /> </a>
