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
                <option value='${player.id}'><c:out value="${player.name}"/></option>
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
                    doOpenPlayer($jQ('#selectFlashPlayerDialog').dialog("option", "playlistUrl").replace('#ID#', $jQ("#flashPlayerSelection option:selected").val()));
                }
            }
        });
    });

    function doOpenPlayer(url) {
        var flashPlayer = window.open(url, "MyTunesRssFlashPlayer", "width=600,height=276,resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,hotkeys=no");
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
                doOpenPlayer(url.replace("#ID#", "${config.flashplayer}"));
            </c:otherwise>
        </c:choose>
    }
</script>
