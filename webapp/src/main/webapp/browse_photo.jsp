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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <style type="text/css">

        .thumblist {
        	text-align: center;
            padding: 15px 8px 0 8px;
            background-color: #f4f4f4;
            margin: 0;
        }

        .thumblist li {
            vertical-align: top;
            margin: 0 7px 15px 7px;
            padding: 0;
            display: inline-block;
        }

        .thumblist div {
        	display: table-cell;
            text-align: center;
            vertical-align: middle;
            width: 130px;
            height: 130px;
        }

        .thumblist img {
            cursor: pointer;
            border: 1px solid black;
        }

    </style>

    <!--[if IE]>
    <style type="text/css">

        .thumblist li {
            vertical-align: top;
            margin: 0 7px 15px 7px;
            padding: 0;
            display: inline;
        }

        .thumblist div {
            text-align: center;
            width: 130px;
            height: 130px;
        }

        .thumblist span {
            vertical-align: middle;
            height: 130px;
        }

        .thumblist img {
            vertical-align: middle;
            cursor: pointer;
            border: 1px solid black;
        }

    </style>
    <![endif]-->

</head>

<body class="browse">

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
                        <div><span></span><img id="img${photo.imageHash}" src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${photo.imageHash}/size=${imageSize}</mt:encrypt>" onclick="self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${firstPhotoIndex + loopStatus.index}</mt:encrypt>/photosBackUrl=${param.backUrl}/size=' + $jQ('div.content-inner').innerWidth()"></div>
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