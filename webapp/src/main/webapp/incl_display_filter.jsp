<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<table class="displayfilter" cellspacing="0">
    <tr>
        <th class="active" colspan="2"><fmt:message key="filter.title"/></th>
    </tr>
    <tr>
        <td><fmt:message key="filter.text"/>:</td>
        <td class="wide"><input id="filterText" type="text" name="filterText" value="${displayFilter.textFilter}"/></td>
    </tr>
    <c:if test="${filterTypeActive}">
        <tr>
            <td><fmt:message key="filter.type"/>:</td>
            <td class="wide">
                <select id="filterMediaType" name="filterType">
                    <option value="All"><fmt:message key="filter.noRestriction"/></option>
                    <option value="Audio" <c:if test="${displayFilter.mediaType.jspName == 'Audio'}">selected="selected"</c:if>><fmt:message key="filter.typeAudio"/></option>
                    <option value="Video" <c:if test="${displayFilter.mediaType.jspName == 'Video'}">selected="selected"</c:if>><fmt:message key="filter.typeVideo"/></option>
                    <option value="Image" <c:if test="${displayFilter.mediaType.jspName == 'Image'}">selected="selected"</c:if>><fmt:message key="filter.typeImage"/></option>
                    <option value="Other" <c:if test="${displayFilter.mediaType.jspName == 'Other'}">selected="selected"</c:if>><fmt:message key="filter.typeOther"/></option>
                </select>
            </td>
        </tr>
    </c:if>
    <c:if test="${filterProtectionActive}">
        <tr>
            <td><fmt:message key="filter.protection"/>:</td>
            <td class="wide">
                <select id="filterProtected" name="filterProtected">
                    <option value="All"><fmt:message key="filter.noRestriction"/></option>
                    <option value="Protected" <c:if test="${displayFilter.protection eq 'Protected'}">selected="selected"</c:if>><fmt:message key="filter.protProtected"/></option>
                    <option value="Unprotected" <c:if test="${displayFilter.protection eq 'Unprotected'}">selected="selected"</c:if>><fmt:message key="filter.protUnprotected"/></option>
                </select>
            </td>
        </tr>
    </c:if>
    <tr>
        <td colspan="2">
            <input type="button" value="<fmt:message key="filter.apply"/>" onclick="self.document.location.href='${displayFilterUrl}/' + getElementParams('filterText,filterType,filterProtected', '/')"/>
        </td>
    </tr>
</table>