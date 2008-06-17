<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:if test="${!empty editablePlaylists}">
    <div id="editPlaylist" class="dialogbox">
        <div class="dialogMessage">
            <fmt:message key="dialog.editPlaylist"/>
        </div>
        <div class="dialogMessage">
            <select id="playlistSelection" style="width:100%">
                <c:forEach items="${editablePlaylists}" var="playlist">
                    <option value='${servletUrl}/continueExistingPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=true/playlist=${playlist.id}/useBackUrl=true/backUrl=${mtfn:encode64(backUrl)}</mt:encrypt>'><c:out value="${playlist.name}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="dialogButton">
            <input type="button" onclick="clickDialog(2)" value="<fmt:message key="doCancel" />" />
        </div>
        <div class="dialogButton">
            <input type="button" onclick="clickDialog(1)" value="<fmt:message key="edit" />" />
        </div>
        <div class="dialogButton">
            <input type="button" onclick="clickDialog(0)" value="<fmt:message key="new" />" />
        </div>
    </div>
</c:if>
