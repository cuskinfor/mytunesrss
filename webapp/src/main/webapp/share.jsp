<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="permFeedServletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="trackinfo">

<div class="body">

<div class="head">
    <h1>
        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
        <span><fmt:message key="myTunesRss"/></span>
    </h1>
</div>

<div class="content">

<div class="content-inner">

<jsp:include page="/incl_error.jsp"/>

<ul class="menu">
    <c:if test="${!empty param.backUrl}">
        <li class="back">
            <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
        </li>
    </c:if>
</ul>

    Info: "${param.text}"<br />

    Facebook<br/>

    Twitter<br/>

    Playlist: ${param.playlist}<br />
    Jukebox: ${param.player}<br />
    Download: ${param.download}<br />
    RSS: ${param.rss}<br />

</div>

</div>

<div class="footer">
    <div class="inner"></div>
</div>

</div>

<jsp:include page="incl_select_flashplayer_dialog.jsp"/>

</body>

</html>