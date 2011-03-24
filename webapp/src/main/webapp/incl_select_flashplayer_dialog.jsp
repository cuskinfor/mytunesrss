<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<div id="selectFlashPlayerDialog" style="display:none;border:solid 2px black;border-radius:6px;background-color:#FFF">
    <h2>
        <fmt:message key="selectFlashPlayerDialogTitle"/>
    </h2>
    <div style="margin:10px">
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
        <p>
            <button onclick="doOpenPlayer()"><fmt:message key="doOpenFlashPlayer"/></button>
            <button onclick="$jQ.modal.close()"><fmt:message key="doCancel"/></button>
        </p>
    </div>
    <input id="selectFlashPlayerDialogUrl" type="hidden" />
</div>

<script type="text/javascript">

    function doOpenPlayer() {
        var val = $jQ("#flashPlayerSelection option:selected").val().split(",");
        var url = $jQ('#selectFlashPlayerDialogUrl').val().replace(/#ID#/, val[0]);
        var width = val[1];
        var height = val[2];
        $jQ.modal.close();
        var flashPlayer = window.open(url, "MyTunesRssFlashPlayer", "width=" + width + ",height=" + height + ",resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,hotkeys=no");
        flashPlayer.onload = function() {
            flashPlayer.document.title = self.document.title;
        }
    }

    function openPlayer(url) {
        <c:choose>
            <c:when test="${empty config.flashplayer}">
                $jQ("#selectFlashPlayerDialogUrl").val(url);
                $jQ("#selectFlashPlayerDialog").modal();
            </c:when>
            <c:otherwise>
                doOpenPlayer(url.replace(/#ID#/, "${config.flashplayer}"), ${mtfn:flashPlayerConfig(config.flashplayer).width}, ${mtfn:flashPlayerConfig(config.flashplayer).height});
            </c:otherwise>
        </c:choose>
    }
</script>
