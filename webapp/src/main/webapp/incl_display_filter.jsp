<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="displayFilter" type="de.codewave.mytunesrss.command.DisplayFilter"--%>
<%--@elvariable id="filterYearActive" type="boolean"--%>
<%--@elvariable id="filterAlbumTypeActive" type="boolean"--%>
<%--@elvariable id="filterTypeActive" type="boolean"--%>
<%--@elvariable id="filterProtectionActive" type="boolean"--%>
<%--@elvariable id="displayFilterUrl" type="java.lang.String"--%>

<div id="displayfilter" class="displayfilter" style="display:${cwfn:choose(empty displayFilter.textFilter && ((displayFilter.minYear == -1 && displayFilter.maxYear == -1) || !filterYearActive) && (displayFilter.albumType == 'ALL' || !filterAlbumTypeActive), 'none', 'block')}">

	<input type="button" value="<fmt:message key="filter.apply"/>" onclick="self.document.location.href='${displayFilterUrl}/' + getElementParams('filterText,filterType,filterProtected,filterMinYear,filterMaxYear,filterAlbumType', '/');"/>
    
    <fmt:message key="filter.text"/>:
    <input id="filterText" type="text" name="filterText" value="${displayFilter.textFilter}"/>

    <c:choose>
        <c:when test="${filterYearActive}">
	        <fmt:message key="filter.year"/>:
	        <input id="filterMinYear" type="text" name="filterMinYear" value="${cwfn:choose(displayFilter.minYear != -1, displayFilter.minYear, '')}"/>
	        -
	        <input id="filterMaxYear" type="text" name="filterMaxYear" value="${cwfn:choose(displayFilter.maxYear != -1, displayFilter.maxYear, '')}"/>
        </c:when>
        <c:otherwise>
            <input id="filterMinYear" type="hidden" name="filterMinYear" value="${cwfn:choose(displayFilter.minYear != -1, displayFilter.minYear, '')}"/>
            <input id="filterMaxYear" type="hidden" name="filterMaxYear" value="${cwfn:choose(displayFilter.maxYear != -1, displayFilter.maxYear, '')}"/>
        </c:otherwise>
    </c:choose>

    <c:choose>
        <c:when test="${filterAlbumTypeActive}">
            <br /><fmt:message key="filter.albumType"/>:
            <select id="filterAlbumType" name="filterAlbumType">
                <option value="ALL" <c:if test="${displayFilter.albumType == 'ALL'}">selected="selected"</c:if>><fmt:message key="filter.albumType.all"/></option>
                <option value="ALBUMS" <c:if test="${displayFilter.albumType == 'ALBUMS'}">selected="selected"</c:if>><fmt:message key="filter.albumType.albums"/></option>
                <option value="COMPILATIONS" <c:if test="${displayFilter.albumType == 'COMPILATIONS'}">selected="selected"</c:if>><fmt:message key="filter.albumType.compilations"/></option>
            </select>
        </c:when>
        <c:otherwise>
            <input id="filterAlbumType" type="hidden" name="filterAlbumType" value="${displayFilter.albumType}"/>
        </c:otherwise>
    </c:choose>

    <%--
        <fmt:message key="filter.type"/>:
            <select id="filterMediaType" name="filterType">
                <option value=""><fmt:message key="filter.noRestriction"/></option>
                <option value="Audio" <c:if test="${displayFilter.mediaType == 'Audio'}">selected="selected"</c:if>><fmt:message key="filter.typeAudio"/></option>
                <option value="Video" <c:if test="${displayFilter.mediaType == 'Video'}">selected="selected"</c:if>><fmt:message key="filter.typeVideo"/></option>
                <option value="Image" <c:if test="${displayFilter.mediaType == 'Image'}">selected="selected"</c:if>><fmt:message key="filter.typeImage"/></option>
                <option value="Other" <c:if test="${displayFilter.mediaType == 'Other'}">selected="selected"</c:if>><fmt:message key="filter.typeOther"/></option>
            </select>
        <fmt:message key="filter.protection"/>:
            <select id="filterProtected" name="filterProtected">
                <option value=""><fmt:message key="filter.noRestriction"/></option>
                <option value="Protected" <c:if test="${displayFilter.protection eq 'Protected'}">selected="selected"</c:if>><fmt:message key="filter.protProtected"/></option>
                <option value="Unprotected" <c:if test="${displayFilter.protection eq 'Unprotected'}">selected="selected"</c:if>><fmt:message key="filter.protUnprotected"/></option>
            </select>
    --%>
</div>