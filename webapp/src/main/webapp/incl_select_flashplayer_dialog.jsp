<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<div id="selectFlashPlayerDialog" title="<fmt:message key="selectFlashPlayerDialogTitle"/>" style="display:none">
    <p>
        <fmt:message key="dialog.selectFlashPlayer"/>
    </p>
    <p>
        <select id="flashPlayerSelection" style="width:100%">
            <c:forEach items="${mtfn:flashPlayerConfigs()}" var="player">
                <option value='${player.id},${player.width},${player.height}'><c:out value="${player.name}"/></option>
            </c:forEach>
        </select>
    </p>
</div>

<script type="text/javascript">
    $jQ(document).ready(function() {
        $jQ("#selectFlashPlayerDialog").dialog({
            autoOpen:false,
            modal:true,
            buttons:{
                "<fmt:message key="doCancel"/>" : function() {
                    $jQ("#selectFlashPlayerDialog").dialog("close");
                },
                "<fmt:message key="doOpenFlashPlayer"/>" : function() {
                    $jQ("#selectFlashPlayerDialog").dialog("close");
                    var val = $jQ("#flashPlayerSelection option:selected").val().split(",");
                    doOpenPlayer($jQ('#selectFlashPlayerDialog').dialog("option", "playlistUrl").replace('#ID#', val[0]), val[1], val[2]);
                }
            }
        });
    });

    function doOpenPlayer(url, width, height) {
        var flashPlayer = window.open(url, "MyTunesRssFlashPlayer", "width=" + width + ",height=" + height + ",resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,hotkeys=no");
        flashPlayer.onload = function() {
            flashPlayer.document.title = self.document.title;
        }
    }

    function openPlayer(url) {
        <c:choose>
            <c:when test="${empty config.flashplayer}">
                $jQ("#selectFlashPlayerDialog").dialog("option", "playlistUrl", url);
                $jQ("#selectFlashPlayerDialog").dialog("open");
            </c:when>
            <c:otherwise>
                doOpenPlayer(url.replace("#ID#", "${config.flashplayer}", ${mtfn:flashPlayerConfig(config.flashplayer).width}, ${mtfn:flashPlayerConfig(config.flashplayer).height}));
            </c:otherwise>
        </c:choose>
    }
</script>
