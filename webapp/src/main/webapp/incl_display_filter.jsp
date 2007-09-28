<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<table class="displayfilter" cellspacing="0">
    <tr>
        <th class="active" colspan="2">Anzeigefilter</th>
    </tr>
    <tr>
        <td>Text:</td>
        <td class="wide"><input id="filterText" type="text" name="filterText" value="${displayFilter.textFilter}"/></td>
    </tr>
    <c:if test="${filterTypeActive}">
        <tr>
            <td>Typ:</td>
            <td class="wide">
                <select id="filterType" name="filterType">
                    <option value="All">keine Einschränkung</option>
                    <option value="Audio" <c:if test="${displayFilter.type eq 'Audio'}">selected="selected"</c:if>>nur Audiodateien</option>
                    <option value="Video" <c:if test="${displayFilter.type eq 'Video'}">selected="selected"</c:if>>nur Videodateien</option>
                </select>
            </td>
        </tr>
    </c:if>
    <c:if test="${filterProtectionActive}">
        <tr>
            <td>Schutz:</td>
            <td class="wide">
                <select id="filterProtected" name="filterProtected">
                    <option value="All">keine Einschränkung</option>
                    <option value="Protected" <c:if test="${displayFilter.protection eq 'Protected'}">selected="selected"</c:if>>nur geschützte Dateien</option>
                    <option value="Unprotected" <c:if test="${displayFilter.protection eq 'Unprotected'}">selected="selected"</c:if>>nur freie Dateien</option>
                </select>
            </td>
        </tr>
    </c:if>
    <tr>
        <td colspan="2">
            <input type="button" value="Filter anwenden" onclick="self.document.location.href='${displayFilterUrl}/' + getElementParams('filterText,filterType,filterProtected', '/')"/>
        </td>
    </tr>
</table>