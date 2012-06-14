<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%@ attribute name="index" required="true" type="java.lang.Integer" %>
<%@ attribute name="backUrl" required="true" %>
<%@ attribute name="linkFragment" required="true" %>
<%@ attribute name="filename" required="true" %>
<%@ attribute name="zipFileCount" required="false" type="java.lang.Integer" %>
<%@ attribute name="track" required="false" type="de.codewave.mytunesrss.datastore.statement.Track" %>
<%@ attribute name="externalSitesFlag" required="false" type="java.lang.Boolean" %>
<%@ attribute name="editTagsResource" required="false" type="java.lang.String" %>
<%@ attribute name="editTagsParams" required="false" type="java.lang.String" %>
<%@ attribute name="defaultPlaylistName" required="false" type="java.lang.String" %>
<%@ attribute name="shareText" required="false" type="java.lang.String" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<c:if test="${externalSitesFlag}">
    <a id="fn_externalsites${index}" class="links" title="<fmt:message key="tooltip.externalSites"/>" <c:if test="${!config.showExternalSites}">style="display:none"</c:if> onclick="openExternalSitesDialog($jQ('#functionsDialogName${index}').text()); return false;"><span>External Sites</span></a>
    <c:if test="${!config.showExternalSites}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.editTags && !empty editTagsResource && !empty editTagsParams}">
	<a id="fn_edittags${index}" class="tags" <c:if test="${!config.showEditTags}">style="display:none"</c:if> onclick="openEditTagsDialog(${editTagsResource}, ${editTagsParams}, $jQ('#functionsDialogName${index}').text());return false" onmouseover="showEditTagsTooltip(this, ${editTagsResource}, ${editTagsParams});" onmouseout="hideTooltipElement(document.getElementById('tooltip_edittags'));" title="<fmt:message key="tooltip.editTags"/>"><span>Edit Tags</span></a>
    <c:if test="${!config.showEditTags}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.share && !empty shareText}"> <%-- TODO: config.showShare, do we need it? if so => implement it --%>
	<a id="fn_share${index}" class="share" <c:if test="${true}">style="display:none"</c:if> onclick="showShareLink(${index}, '${mtfn:escapeJs(shareText)}')" title="<fmt:message key="tooltip.share"/>"><span>Share</span></a>
    <c:if test="${true}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.createPlaylists && !empty defaultPlaylistName}">
    <a id="fn_oneclickplaylist${index}" class="oneclickplaylist" <c:if test="${!config.showAddToPlaylist}">style="display:none"</c:if> onclick="openAddOneClickPlaylistDialog('${linkFragment}', '${defaultPlaylistName}')" title="<fmt:message key="playlist.addToPlaylist"/>"><span>Add to one click playlist</span></a>
    <c:if test="${!config.showAddToPlaylist}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.remoteControl && globalConfig.remoteControl}">
	<a id="fn_remotecontrol${index}" class="remote" onclick="self.location.href='${servletUrl}/showRemoteControl/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}/backUrl=${backUrl}</mt:encrypt>'" <c:if test="${!config.remoteControl}">style="display:none"</c:if> title="<fmt:message key="tooltip.remotecontrol"/>"><span>Remote</span></a>
    <c:if test="${!config.remoteControl}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.remoteControl && globalConfig.remoteControl && !empty track}">
	<a id="fn_addremotecontrol${index}" class="addremote" onclick="showLoading('<fmt:message key="loading.addRemoteControl"/>');MediaPlayerResource.addToPlaylist({track:'${track.id}',autostart:true});hideLoading();return false" <c:if test="${!config.addRemoteControl}">style="display:none"</c:if> title="<fmt:message key="tooltip.addremotecontrol"/>"><span>Add to remote</span></a>
    <c:if test="${!config.addRemoteControl}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.rss}">
	<a id="fn_rss${index}" class="rss" onclick="self.location.href=$jQ('#fn_rss${index}').attr('href'); return false" href="${permFeedServletUrl}/createRSS/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/${filename}.xml" <c:if test="${!config.showRss}">style="display:none"</c:if> title="<fmt:message key="tooltip.rssfeed"/>"><span>RSS</span></a>
    <c:if test="${!config.showRss}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${authUser.playlist}">
	<a id="fn_playlist${index}" class="playlist" onclick="self.location.href=$jQ('#fn_playlist${index}').attr('href'); return false" href="${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/${filename}.${config.playlistFileSuffix}" <c:if test="${!config.showPlaylist}">style="display:none"</c:if> title="<fmt:message key="tooltip.playlist"/>"><span>Playlist</span></a>
    <c:if test="${!config.showPlaylist}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:if test="${globalConfig.flashPlayer && authUser.player}">
	<a id="fn_player${index}" class="flash" <c:if test="${!config.showPlayer}">style="display:none"</c:if> onclick="openPlayer($jQ('#fn_player${index}').attr('href')); return false" href="${servletUrl}/showJukebox/${auth}/<mt:encrypt key="${encryptionKey}">playlistParams=<mt:encode64>${linkFragment}</mt:encode64></mt:encrypt>/playerId=" title="<fmt:message key="tooltip.flashplayer"/>"><span>Flash Player</span></a>
    <c:if test="${!config.showPlayer}"><c:set var="displayMenu" value="true"/></c:if>
</c:if>
<c:choose>
    <c:when test="${!empty track && authUser.yahooPlayer && config.yahooMediaPlayer && mtfn:lowerSuffix(pageContext, config, authUser, track) eq 'mp3'}">
        <c:set var="yahoo" scope="request" value="true"/>
        <a id="fn_yahoo${index}" class="htrack" href="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>" title="<c:out value="${track.name}"/>" style="display:none">
            <img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${track.imageHash}/size=128</mt:encrypt>" style="display:none" alt=""/>
        </a>
        <c:if test="${!empty track && authUser.download}">
            <a style="display:none" id="fn_download${index}" class="download" onclick="self.document.location.href=$jQ('#fn_download${index}').attr('href'); return false" href="<c:out value="${mtfn:downloadLink(pageContext, track, null)}"/>" title="<fmt:message key="tooltip.playtrack"/>"><span>Download</span></a>
        </c:if>
    </c:when>
    <c:when test="${!empty track && authUser.download}">
        <a id="fn_download${index}" class="download" onclick="self.document.location.href=$jQ('#fn_download${index}').attr('href'); return false" href="<c:out value="${mtfn:downloadLink(pageContext, track, null)}"/>" <c:if test="${!config.showDownload}">style="display:none"</c:if> title="<fmt:message key="tooltip.playtrack"/>"><span>Download</span></a>
    </c:when>
    <c:when test="${empty track && authUser.download && (authUser.maximumZipEntries <= 0 || zipFileCount <= authUser.maximumZipEntries)}">
        <a id="fn_download${index}" class="download" onclick="self.document.location.href=$jQ('#fn_download${index}').attr('href'); return false" href="${servletUrl}/getZipArchive/${auth}/<mt:encrypt key="${encryptionKey}">${linkFragment}</mt:encrypt>/${filename}.zip" <c:if test="${!config.showDownload}">style="display:none"</c:if> title="<fmt:message key="tooltip.downloadzip"/>"><span>Download</span></a>
    </c:when>
    <c:when test="${empty track && authUser.download && authUser.maximumZipEntries > 0 && zipFileCount > authUser.maximumZipEntries}">
        <a id="fn_download${index}" class="download" onclick="displayError('<fmt:message key="error.zipLimit"><fmt:param value="${authUser.maximumZipEntries}"/></fmt:message>');" <c:if test="${!config.showDownload}">style="display:none"</c:if> title="<fmt:message key="tooltip.downloadzip"/>"><span>Download</span></a>
    </c:when>
</c:choose>
<c:if test="${displayMenu}">
    <a id="functionsMenu${index}" onclick="openFunctionsMenu(${index}, $jQ('#functionsDialogName${index}').text());" class="menu" title="<fmt:message key="tooltip.functionsMenu"/>"><span>Menu</span></a>
</c:if>
