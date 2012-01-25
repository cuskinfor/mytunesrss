<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="mttag" %>

<%--
  ~ Copyright (c) 2011. Codewave Software Michael Descher.
  ~ All rights reserved.
  --%>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="photoAlbums" type="java.util.List<de.codewave.mytunesrss.datastore.statement.PhotoAlbum>"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browsePhotoAlbum/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        function downloadAlbum(params, size) {
            $jQ.modal.close();
            self.document.location.href='${servletUrl}/downloadPhotoAlbum/${auth}/' + params + "/size=" + size;
        }
        function openSizeSelectionDialog(params) {
            $jQ("#sizeSelectionDialog").data("uriParams", params);
            openDialog("#sizeSelectionDialog");
        }
    </script>

</head>

<body class="browse">

    <div class="body">

        <div class="head">
            <h1 class="browse">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>

        <div class="content">

            <div class="content-inner">

                <ul class="menu">
                    <li class="back">
                        <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                    </li>
                </ul>

                <jsp:include page="/incl_error.jsp" />

                <table cellspacing="0" class="tracklist searchResult">
                <c:set var="fnCount" value="0" />
                <c:forEach items="${photoAlbums}" var="photoAlbum" varStatus="loopStatus">
                <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                    <td class="artist">
                        <div class="trackName">
                            <a id="functionsDialogName${fnCount}"
                               href="${servletUrl}/browsePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${mtfn:encode64(photoAlbum.name)}/photoalbumid=${mtfn:encode64(photoAlbum.id)}</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"
                               onmouseover="showTooltip(this)"
                               onmouseout="hideTooltip(this)"
							   class="photo">
                                <c:out value="${photoAlbum.name}" />
                                ${mtfn:dates(pageContext.request, "(", photoAlbum.firstDate, " - ", photoAlbum.lastDate, ")")}
                            </a>
                        </div>
                    </td>
                    <c:if test="${authUser.downloadPhotoAlbum}">
                        <td class="actions">
                            <a class="download" onclick="openSizeSelectionDialog('<mt:encrypt key="${encryptionKey}">photoalbum=${mtfn:encode64(photoAlbum.name)}/photoalbumid=${mtfn:encode64(photoAlbum.id)}</mt:encrypt>')"></a>
                        </td>
                    </c:if>
                </tr>
                <c:set var="fnCount" value="${fnCount + 1}"/>
                </c:forEach>
                </table>

                <c:if test="${!empty pager}">
                    <c:set var="pagerCommand"
                           scope="request">${servletUrl}/browsePhotoAlbum/${auth}/index={index}/backUrl=${param.backUrl}</c:set>
                    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
                    <jsp:include page="incl_bottomPager.jsp" />
                </c:if>

            </div>

        </div>

        <div class="footer">
            <div class="inner"></div>
        </div>

    </div>

    <jsp:include page="incl_select_flashplayer_dialog.jsp"/>

    <jsp:include page="incl_functions_menu.jsp"/>

    <div id="sizeSelectionDialog" class="dialog">
        <h2>
            <fmt:message key="dialog.selectPhotoSizeForAlbumDownload.title" />
        </h2>
        <div>
            <p>
                <fmt:message key="dialog.selectPhotoSizeForAlbumDownload.message" />
            </p>
            <p>
                <select id="photoSizeSelector" name="photoSize" onchange="resize()">
                    <option value="25"><fmt:message key="photos.size.25" /></option>
                    <option <c:if test="${config.photoSize == 50}">selected="selected"</c:if> value="50"><fmt:message key="photos.size.50" /></option>
                    <option <c:if test="${config.photoSize == 75}">selected="selected"</c:if> value="75"><fmt:message key="photos.size.75" /></option>
                    <option <c:if test="${config.photoSize == 100}">selected="selected"</c:if> value="100"><fmt:message key="photos.size.100" /></option>
                </select>
            </p>
            <p align="right">
                <button onclick="$jQ.modal.close()"><fmt:message key="doCancel"/></button>
                <button onclick="downloadAlbum($jQ('#sizeSelectionDialog').data('uriParams'), $jQ('#sizeSelectionDialog select option:selected').val())"><fmt:message key="dialog.selectPhotoSizeForAlbumDownload.ok"/></button>
            </p>
        </div>
    </div>

</body>

</html>