<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<%--@elvariable id="editablePlaylists" type="java.util.List"--%>

<div id="functions" style="display:none" class="actionsMenu">
    <a id="functions_externalsites" class="links" onclick="functionMenuClick('externalsites');">dummy</a>
    <a id="functions_edittags" class="tags" onclick="functionMenuClick('edittags');">dummy</a>
    <a id="functions_remotecontrol" class="remote" onclick="functionMenuClick('remotecontrol');">dummy</a>
    <a id="functions_rss" class="rss" onclick="functionMenuClick('rss');">dummy</a>
    <a id="functions_playlist" class="playlist" onclick="functionMenuClick('playlist');">dummy</a>
    <a id="functions_player" class="flash" onclick="functionMenuClick('player');">dummy</a>
    <a id="functions_download" class="download" onclick="functionMenuClick('download');">dummy</a>
    <a id="functions_oneclickplaylist" class="oneclickplaylist" onclick="functionMenuClick('oneclickplaylist');">dummy</a>
</div>

<script type="text/javascript">
    function openFunctionsMenu(index, title) {
        $jQ('#functions').dialog('option', 'functionIndex', index);
        $jQ('#functions').dialog('option', 'title', title);
        $jQ('#functions_externalsites').text($jQ('#fn_externalsites' + index).attr('title'));
        $jQ('#functions_edittags').text($jQ('#fn_edittags' + index).attr('title'));
        $jQ('#functions_remotecontrol').text($jQ('#fn_remotecontrol' + index).attr('title'));
        $jQ('#functions_rss').text($jQ('#fn_rss' + index).attr('title'));
        $jQ('#functions_playlist').text($jQ('#fn_playlist' + index).attr('title'));
        $jQ('#functions_player').text($jQ('#fn_player' + index).attr('title'));
        $jQ('#functions_download').text($jQ('#fn_download' + index).attr('title'));
        $jQ('#functions_oneclickplaylist').text($jQ('#fn_oneclickplaylist' + index).attr('title'));
        $jQ('#functions').dialog('open');
    }
    function functionMenuClick(name) {
        $jQ('#functions').dialog('close');
        if (name === 'addToPlaylist') {

        } else {
            if ($jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).attr('href') == undefined) {
                $jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).trigger('click');
            } else {
                self.document.location.href = $jQ('#fn_' + name + $jQ('#functions').dialog('option', 'functionIndex')).attr('href');
            }
        }
    }
    function showHideLink(name) {
        if ($jQ("#fn_" + name + $jQ('#functions').dialog('option', 'functionIndex')).length == 0) {
             $jQ("#functions_" + name).css("display", "none");
        } else {
            $jQ("#functions_" + name).css("display", "block");
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
            showHideLink("oneclickplaylist");
        }
    });

</script>
