<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:if test="${!empty editablePlaylists}">
    <div id="editPlaylistDialog" class="dialog">
        <h2>
            <fmt:message key="editPlaylistDialogTitle"/>
        </h2>
        <div>
            <p>
                <fmt:message key="dialog.editPlaylist"/>
            </p>
            <p>
                <select id="playlistSelection" style="width:100%">
                    <c:forEach items="${editablePlaylists}" var="playlist">
                        <option value='${playlist.id}'><c:out value="${playlist.name}"/></option>
                    </c:forEach>
                </select>
            </p>
            <p align="right">
                <button onclick="$jQ.modal.close()"><fmt:message key="doCancel"/></button>
                <button onclick="editPlaylistDialog_edit()"><fmt:message key="edit"/></button>
                <button onclick="editPlaylistDialog_new()"><fmt:message key="new"/></button>
            </p>
        </div>
    </div>
    <script type="text/javascript">
        function editPlaylistDialog_edit() {
            jsonRpc('${servletUrl}', "EditPlaylistService.startEditPlaylist", [$jQ("#playlistSelection option:selected").val()], function() {
                document.location.href = "${backUrl}";
            }, "${remoteApiSessionId}");
            $jQ.modal.close();
        }
        function editPlaylistDialog_new() {
            jsonRpc('${servletUrl}', "EditPlaylistService.startEditPlaylist", [null], function() {
                document.location.href = "${backUrl}";
            }, "${remoteApiSessionId}");
            $jQ.modal.close();
        }
    </script>
</c:if>

<div id="addOneClickPlaylistDialog" class="dialog">
    <h2>
        <fmt:message key="editPlaylistDialogTitle"/>
    </h2>
    <div>
        <p>
            <fmt:message key="dialog.addToPlaylistOneClickSelect"/>
        </p>
        <p>
            <select id="addOneClickPlaylistDialogPlaylistSelection" style="width:100%">
                <c:forEach items="${editablePlaylists}" var="playlist">
                    <option value='${playlist.id}'><c:out value="${playlist.name}"/></option>
                </c:forEach>
            </select>
        </p>
        <p>
            <fmt:message key="dialog.addToPlaylistOneClickEnter"/>
        </p>
        <p>
            <input id="addOneClickPlaylistDialogPlaylistEnter" style="width:100%" type="text" />
        </p>
        <p align="right">
            <button onclick="$jQ.modal.close()"><fmt:message key="doCancel"/></button>
            <button onclick="addOneClickPlaylistDialog_add"><fmt:message key="addToPlaylistOneClick"/></button>
            <button onclick="addOneClickPlaylistDialog_new"><fmt:message key="createPlaylistOneClick"/></button>
        </p>
    </div>
</div>
<script type="text/javascript">
    function addOneClickPlaylistDialog_add() {
        document.location.href = "${servletUrl}/addToOneClickPlaylist/${auth}/" + $jQ("#addOneClickPlaylistDialog").data("linkFragment") + "/playlistId=" + $jQ("#addOneClickPlaylistDialogPlaylistSelection option:selected").val() + "/backUrl=${mtfn:encode64(backUrl)}";
        $jQ.modal.close();
    }
    function addOneClickPlaylistDialog_new() {
        if ($jQ("#addOneClickPlaylistDialogPlaylistEnter").val() != '') {
            document.location.href = "${servletUrl}/addToOneClickPlaylist/${auth}/playlistName=" + escape($jQ("#addOneClickPlaylistDialogPlaylistEnter").val()) + "/" + $jQ("#addOneClickPlaylistDialog").dialog("option", "linkFragment") + "/backUrl=${mtfn:encode64(backUrl)}";
            $jQ.modal.close();
        } else {
            alert("TODO i18n: enter a name first!")
        }
    }
    function openAddOneClickPlaylistDialog(linkFragment, newPlaylistName) {
        $jQ("#addOneClickPlaylistDialog").data("linkFragment", linkFragment);
        $jQ("#addOneClickPlaylistDialogPlaylistEnter").val(newPlaylistName);
        openDialog("#addOneClickPlaylistDialog");
    }
</script>
