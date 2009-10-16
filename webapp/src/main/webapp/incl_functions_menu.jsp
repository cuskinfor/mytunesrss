<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div id="functions" style="display:none">
    <a id="functions_externalsites" style="cursor:pointer" onclick="functionMenuClick('externalsites')">
        <img src="${appUrl}/images/http.gif" />&nbsp;<fmt:message key="tooltip.externalSites"/>
    </a>
    <br />
    <a id="functions_edittags" style="cursor:pointer" onclick="functionMenuClick('edittags')">
        <img src="${appUrl}/images/http.gif" />&nbsp;<fmt:message key="tooltip.editTags"/>
    </a>
    <br />
    <a id="functions_remotecontrol" style="cursor:pointer" onclick="functionMenuClick('remotecontrol')">
        <img src="${appUrl}/images/remote_control.gif" />&nbsp;<fmt:message key="tooltip.remotecontrol"/>
    </a>
    <br />
    <a id="functions_rss" style="cursor:pointer" onclick="functionMenuClick('rss')">
        <img src="${appUrl}/images/rss.gif" />&nbsp;<fmt:message key="tooltip.rssfeed"/>
    </a>
    <br />
    <a id="functions_playlist" style="cursor:pointer" onclick="functionMenuClick('playlist')">
        <img src="${appUrl}/images/playlist.gif" />&nbsp;<fmt:message key="tooltip.playlist"/>
    </a>
    <br />
    <a id="functions_player" style="cursor:pointer" onclick="functionMenuClick('player')">
        <img src="${appUrl}/images/player.gif" />&nbsp;<fmt:message key="tooltip.flashplayer"/>
    </a>
    <br />
    <a id="functions_download" style="cursor:pointer" onclick="functionMenuClick('download')">
        <img src="${appUrl}/images/download.gif" />&nbsp;<fmt:message key="tooltip.downloadzip"/>
    </a>
</div>

<script type="text/javascript">
    function openFunctionsMenu(index, title) {
        $jQ('#functions').dialog('option', 'functionIndex', index);
        $jQ('#functions').dialog('option', 'title', title);
        $jQ('#functions').dialog('open');
    }
    function functionMenuClick(name) {
        $jQ('#functions').dialog('close');
        if ($jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).attr('href') == undefined) {
            $jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).trigger('click')
        } else {
            self.document.location.href = $jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).attr('href')
        }
    }
    function showHideLink(name) {
        if ($jQ("#fn_" + name + $jQ('#functions').dialog('option', 'functionIndex')).length == 0) {
             $jQ("#functions_" + name).css("display", "none");
        } else {
            $jQ("#functions_" + name).css("display", "inline");
        }
    }
    $jQ("#functions").dialog({
        autoOpen:false,
        modal:true,
        width:350,
        open:function(event, ui) {
            showHideLink("externalsites");
            showHideLink("remotecontrol");
            showHideLink("rss");
            showHideLink("playlist");
            showHideLink("player");
            showHideLink("download");
        }
    });
</script>
