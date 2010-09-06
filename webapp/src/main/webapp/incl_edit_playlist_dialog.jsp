<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:if test="${!empty editablePlaylists}">
    <div id="editPlaylistDialog" title="<fmt:message key="editPlaylistDialogTitle"/>" style="display:none">
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
    </div>
    <script type="text/javascript">
        $jQ(document).ready(function() {
            $jQ("#editPlaylistDialog").dialog({
                autoOpen:false,
                modal:true,
                buttons:{
                    "<fmt:message key="doCancel"/>" : function() {
                        $jQ("#editPlaylistDialog").dialog("close");
                    },
                    "<fmt:message key="edit"/>" : function() {
                        $jQ("#editPlaylistDialog").dialog("close");
                        jsonRpc('${servletUrl}', "EditPlaylistService.startEditPlaylist", [$jQ("#playlistSelection option:selected").val()], function() {
                            document.location.href = "${backUrl}";
                        }, "${remoteApiSessionId}");
                    },
                    "<fmt:message key="new"/>" : function() {
                        $jQ("#editPlaylistDialog").dialog("close");
                        jsonRpc('${servletUrl}', "EditPlaylistService.startEditPlaylist", [null], function() {
                            document.location.href = "${backUrl}";
                        }, "${remoteApiSessionId}");
                    }
                }
            });
        });
    </script>
</c:if>

<div id="addOneClickPlaylistDialog" title="<fmt:message key="editPlaylistDialogTitle"/>" style="display:none">
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
</div>
<script type="text/javascript">
    $jQ(document).ready(function() {
        $jQ("#addOneClickPlaylistDialog").dialog({
            autoOpen:false,
            modal:true,
            buttons:{
                "<fmt:message key="doCancel"/>" : function() {
                    $jQ("#addOneClickPlaylistDialog").dialog("close");
                },
                "<fmt:message key="addToPlaylistOneClick"/>" : function() {
                    $jQ("#addOneClickPlaylistDialog").dialog("close");
                    document.location.href = "${servletUrl}/addToOneClickPlaylist/${auth}/" + $jQ("#addOneClickPlaylistDialog").dialog("option", "linkFragment") + "/playlistId=" + $jQ("#addOneClickPlaylistDialogPlaylistSelection option:selected").val() + "/backUrl=${mtfn:encode64(backUrl)}";
                },
                "<fmt:message key="createPlaylistOneClick"/>" : function() {
                    if ($jQ("#addOneClickPlaylistDialogPlaylistEnter").val() != '') {
                        $jQ("#addOneClickPlaylistDialog").dialog("close");
                        document.location.href = "${servletUrl}/addToOneClickPlaylist/${auth}/playlistName=" + escape($jQ("#addOneClickPlaylistDialogPlaylistEnter").val()) + "/" + $jQ("#addOneClickPlaylistDialog").dialog("option", "linkFragment") + "/backUrl=${mtfn:encode64(backUrl)}";
                    } else {
                        alert("TODO i18n: enter a name first!")
                    }
                }
            }
        });
    });

    function openAddOneClickPlaylistDialog(linkFragment, newPlaylistName) {
        $jQ("#addOneClickPlaylistDialog").dialog("option", "linkFragment", linkFragment);
        $jQ("#addOneClickPlaylistDialogPlaylistEnter").val(newPlaylistName);
        $jQ("#addOneClickPlaylistDialog").dialog("open");
    }

</script>
