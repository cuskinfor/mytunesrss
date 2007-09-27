<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<table class="displayfilter" cellspacing="0">
    <tr>
        <th class="active" colspan="2">Anzeigefilter</th>
    </tr>
    <tr>
        <td>Text:</td>
        <td class="wide"><input id="filterText" type="text" name="filterText" value="${displayFilter.text}"/></td>
    </tr>
    <tr>
        <td>Typ:</td>
        <td class="wide">
            <select id="filterType" name="filterType">
                <option value="">keine Einschränkung</option>
                <option value="audio" <c:if test="${displayFilter.type eq 'audio'}">selected="selected"</c:if>>nur Audiodateien</option>
                <option value="video" <c:if test="${displayFilter.type eq 'video'}">selected="selected"</c:if>>nur Videodateien</option>
            </select>
        </td>
    </tr>
    <tr>
        <td>Schutz:</td>
        <td class="wide">
            <select id="filterProtected" name="filterProtected">
                <option value="">keine Einschränkung</option>
                <option value="protected" <c:if test="${displayFilter.protected eq 'protected'}">selected="selected"</c:if>>nur geschützte Dateien</option>
                <option value="unprotected" <c:if test="${displayFilter.protected eq 'unprotected'}">selected="selected"</c:if>>nur freie Dateien</option>
            </select>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <input type="button" value="Filter anwenden" onclick="self.document.location.href='${servletUrl}/editPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">allowEditEmpty=${param.allowEditEmpty}</mt:encrypt>/index=${param.index}/backUrl=${param.backUrl}/' + getElementParams('filterText,filterType,filterProtected', '/')"/>
        </td>
    </tr>
</table>