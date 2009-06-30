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
