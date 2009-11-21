<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="displayFilter" type="de.codewave.mytunesrss.command.DisplayFilter"--%>
<%--@elvariable id="filterYearActive" type="boolean"--%>
<%--@elvariable id="filterTypeActive" type="boolean"--%>
<%--@elvariable id="filterProtectionActive" type="boolean"--%>
<%--@elvariable id="displayFilterUrl" type="java.lang.String"--%>

<table class="displayfilter" cellspacing="0">
    <tr id="displayfilterHeaderLine">
        <th class="active" colspan="2"><fmt:message key="filter.title"/><img style="float:right" src="${appUrl}/images/refresh.png" alt="" onclick="$jQ('#displayfilter').slideToggle();" /></th>
    </tr>
</table>
<table id="displayfilter" class="displayfilter" cellspacing="0" style="display:${cwfn:choose(empty displayFilter.textFilter && ((displayFilter.minYear == -1 && displayFilter.maxYear == -1) || !filterYearActive), 'none', 'block')}">
    <tr>
        <td><fmt:message key="filter.text"/>:</td>
        <td class="wide"><input id="filterText" type="text" name="filterText" value="${displayFilter.textFilter}"/></td>
    </tr>
    <c:choose>
        <c:when test="${filterYearActive}">
            <tr>
                <td><fmt:message key="filter.year"/>:</td>
                <td class="wide"><input id="filterMinYear" type="text" name="filterMinYear" value="${cwfn:choose(displayFilter.minYear != -1, displayFilter.minYear, '')}"/> - <input id="filterMaxYear" type="text" name="filterMaxYear" value="${cwfn:choose(displayFilter.maxYear != -1, displayFilter.maxYear, '')}"/></td>
            </tr>
        </c:when>
        <c:otherwise>
            <input id="filterMinYear" type="hidden" name="filterMinYear" value="${cwfn:choose(displayFilter.minYear != -1, displayFilter.minYear, '')}"/>
            <input id="filterMaxYear" type="hidden" name="filterMaxYear" value="${cwfn:choose(displayFilter.maxYear != -1, displayFilter.maxYear, '')}"/>
        </c:otherwise>
    </c:choose>
    <%--tr>
        <td><fmt:message key="filter.type"/>:</td>
        <td class="wide">
            <select id="filterMediaType" name="filterType">
                <option value=""><fmt:message key="filter.noRestriction"/></option>
                <option value="Audio" <c:if test="${displayFilter.mediaType.jspName == 'Audio'}">selected="selected"</c:if>><fmt:message key="filter.typeAudio"/></option>
                <option value="Video" <c:if test="${displayFilter.mediaType.jspName == 'Video'}">selected="selected"</c:if>><fmt:message key="filter.typeVideo"/></option>
                <option value="Image" <c:if test="${displayFilter.mediaType.jspName == 'Image'}">selected="selected"</c:if>><fmt:message key="filter.typeImage"/></option>
                <option value="Other" <c:if test="${displayFilter.mediaType.jspName == 'Other'}">selected="selected"</c:if>><fmt:message key="filter.typeOther"/></option>
            </select>
        </td>
    </tr>
    <tr>
        <td><fmt:message key="filter.protection"/>:</td>
        <td class="wide">
            <select id="filterProtected" name="filterProtected">
                <option value=""><fmt:message key="filter.noRestriction"/></option>
                <option value="Protected" <c:if test="${displayFilter.protection eq 'Protected'}">selected="selected"</c:if>><fmt:message key="filter.protProtected"/></option>
                <option value="Unprotected" <c:if test="${displayFilter.protection eq 'Unprotected'}">selected="selected"</c:if>><fmt:message key="filter.protUnprotected"/></option>
            </select>
        </td>
    </tr--%>
    <tr>
        <td colspan="2">
            <input type="button" value="<fmt:message key="filter.apply"/>" onclick="self.document.location.href='${displayFilterUrl}/' + getElementParams('filterText,filterType,filterProtected,filterMinYear,filterMaxYear', '/');"/>
        </td>
    </tr>
</table>