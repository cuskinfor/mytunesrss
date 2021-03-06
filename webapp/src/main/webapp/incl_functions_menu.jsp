<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<div id="functionsmenu" class="dialog">
    <h2>MyTunesRSS</h2>
    <div id="functions" class="actionsMenu">
        <a id="functions_externalsites" class="links" onclick="functionMenuClick('externalsites');">dummy</a>
        <a id="functions_share" class="share" onclick="functionMenuClick('share');">dummy</a>
        <a id="functions_remotecontrol" class="remote" onclick="functionMenuClick('remotecontrol');">dummy</a>
        <a id="functions_addremotecontrol" class="addremote" onclick="functionMenuClick('addremotecontrol');">dummy</a>
        <a id="functions_rss" class="rss" onclick="functionMenuClick('rss');">dummy</a>
        <a id="functions_playlist" class="playlist" onclick="functionMenuClick('playlist');">dummy</a>
        <a id="functions_player" class="flash" onclick="functionMenuClick('player');">dummy</a>
        <a id="functions_download" class="download" onclick="functionMenuClick('download');">dummy</a>
        <a id="functions_oneclickplaylist" class="oneclickplaylist" onclick="functionMenuClick('oneclickplaylist');">dummy</a>
    </div>
</div>

<script type="text/javascript">
    function openFunctionsMenu(index, title) {
        $jQ('#functions').data('functionIndex', index);
        $jQ('#functions').data('title', title);
        $jQ('#functions_externalsites').text($jQ('#fn_externalsites' + index).attr('title'));
        $jQ('#functions_share').text($jQ('#fn_share' + index).attr('title'));
        $jQ('#functions_remotecontrol').text($jQ('#fn_remotecontrol' + index).attr('title'));
        $jQ('#functions_addremotecontrol').text($jQ('#fn_addremotecontrol' + index).attr('title'));
        $jQ('#functions_rss').text($jQ('#fn_rss' + index).attr('title'));
        $jQ('#functions_playlist').text($jQ('#fn_playlist' + index).attr('title'));
        $jQ('#functions_player').text($jQ('#fn_player' + index).attr('title'));
        $jQ('#functions_download').text($jQ('#fn_download' + index).attr('title'));
        $jQ('#functions_oneclickplaylist').text($jQ('#fn_oneclickplaylist' + index).attr('title'));
        showHideLink("externalsites");
        showHideLink("share");
        showHideLink("remotecontrol");
        showHideLink("addremotecontrol");
        showHideLink("rss");
        showHideLink("playlist");
        showHideLink("player");
        showHideLink("download");
        showHideLink("oneclickplaylist");
        openDialog('#functionsmenu');
    }
    function functionMenuClick(name) {
        $jQ.modal.close();
        setTimeout(function() {
            $jQ('#fn_' + name + $jQ('#functions').data('functionIndex')).trigger('click')
        }, 10);
    }
    function showHideLink(name) {
        if ($jQ("#fn_" + name + $jQ('#functions').data('functionIndex')).length == 0) {
             $jQ("#functions_" + name).css("display", "none");
        } else {
            $jQ("#functions_" + name).css("display", "block");
        }
    }

</script>

<script type="text/javascript">
    function showShareLink(index, text, imageHash) {
        new $jQ.ajax({
            url : "${servletUrl}/showShareLink/${auth}/<mt:encrypt>backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>",
            type : "POST",
            contentType : "application/x-www-form-urlencoded",
            processData : true,
            data : {
                "text" : text,
                "imageHash" : (imageHash !== undefined ? imageHash : ""),
                "rss" : $jQ("#fn_rss" + index).attr('href'),
                "playlist" : $jQ("#fn_playlist" + index).attr('href'),
                "jukebox" : $jQ("#fn_player" + index).attr("href"),
                "download" : $jQ("#fn_download" + index).attr('href')
            },
            success : function(data) {
                $jQ("#shareLinkDialogContent").html(data);
                openDialog("#shareLinkDialog");
            }
        });

        $jQ("#showShareFormShareText").val(text);
        $jQ("#showShareFormRss").val();
        $jQ("#showShareFormPlaylist").val();
        $jQ("#showShareFormJukebox").val();
        $jQ("#showShareFormDownload").val();
        $jQ("#showShareForm").submit();
    }
</script>

<div id="shareLinkDialog" class="dialog">
    <h2>
        <fmt:message key="tooltip.share"/>
    </h2>

    <div id="shareLinkDialogContent" class="shareLinkDialogContent">
    </div>

</div>
