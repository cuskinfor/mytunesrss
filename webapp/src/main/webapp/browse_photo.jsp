<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="mttag" %>

<c:set var="imageSize" value="128" />  

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.config.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.config.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="photos" type="java.util.List<de.codewave.mytunesrss.datastore.statement.Photo>"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browsePhoto/${auth}/<mt:encrypt key="${encryptionKey}">/photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<head>
    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        function loadThumbnails() {
            <c:forEach items="${photos}" var="photo" varStatus="loopStatus">
                $jQ("#img${photo.id}").attr("src", "${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${photo.imageHash}/photoId=${photo.id}/size=${imageSize}</mt:encrypt>");
            </c:forEach>
        }
    </script>

</head>

<body class="browse" onload="loadThumbnails()">

<div class="body">

    <div class="head">
        <h1 class="browse">
            <c:if test="${sessionAuthorized}">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span id="linkPortal"><fmt:message key="portal"/></span></a>
            </c:if>
            <span><fmt:message key="myTunesRss"/></span>
        </h1>
    </div>

    <div class="content">

        <div class="content-inner">

            <c:if test="${sessionAuthorized}">
                <ul class="menu">
                    <li class="back">
                        <a id="linkBack" href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                    </li>
                </ul>
            </c:if>

            <jsp:include page="/incl_error.jsp" />

            <table cellspacing="0" class="tracklist searchResult">
                <tr>
                    <th class="active"><c:out value="${mtfn:decode64(param.photoalbum)}"/></th>
                </tr>
            </table>

            <ul class="thumblist">
                <c:forEach items="${photos}" var="photo" varStatus="loopStatus">
                    <li>
                        <div><span></span><img id="img${photo.id}" src="${themeUrl}/images/animated_progress.gif" onclick="self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${firstPhotoIndex + loopStatus.index}</mt:encrypt>/photosBackUrl=${param.backUrl}/size=' + $jQ('div.content-inner').innerWidth()" alt="<c:out value="${photo.name}"/>"/></div>
                    </li>
                </c:forEach>
            </ul>
            <c:if test="${!empty pager}">
                <c:set var="pagerCommand"
                       scope="request">${servletUrl}/browsePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
                <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
                <jsp:include page="incl_bottomPager.jsp" />
            </c:if>

        </div>

    </div>

    <div class="footer">
        <div class="inner"></div>
    </div>

</div>

</body>

</html>