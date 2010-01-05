<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>

<div id="functions" style="display:none" class="actionsMenu">
    <a id="functions_externalsites" class="links" onclick="functionMenuClick('externalsites');">
        <fmt:message key="tooltip.externalSites"/>
    </a>
    <a id="functions_edittags" class="tags" onclick="functionMenuClick('edittags');">
        <fmt:message key="tooltip.editTags"/>
    </a>
    <a id="functions_remotecontrol" class="remote" onclick="functionMenuClick('remotecontrol');">
        <fmt:message key="tooltip.remotecontrol"/>
    </a>
    <a id="functions_rss" class="rss" onclick="functionMenuClick('rss');">
        <fmt:message key="tooltip.rssfeed"/>
    </a>
    <a id="functions_playlist" class="playlist" onclick="functionMenuClick('playlist');">
        <fmt:message key="tooltip.playlist"/>
    </a>
    <a id="functions_player" class="flash" onclick="functionMenuClick('player');">
        <fmt:message key="tooltip.flashplayer"/>
    </a>
    <a id="functions_download" class="download" onclick="functionMenuClick('download');">
        <fmt:message key="tooltip.downloadzip"/>
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
            $jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).trigger('click');
        } else {
            self.document.location.href = $jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).attr('href');
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
        open:function() {
            showHideLink("externalsites");
            showHideLink("edittags");
            showHideLink("remotecontrol");
            showHideLink("rss");
            showHideLink("playlist");
            showHideLink("player");
            showHideLink("download");
        }
    });
</script>
