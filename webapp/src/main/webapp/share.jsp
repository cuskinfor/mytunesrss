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

    <script type="text/javascript">
        var links = new Array();

        function displayLink() {
            $jQ("#linkDisplay").text(links[$jQ("#linkSelect option:selected")[0].index]);
        }

        $jQ(document).ready(function() {
            links[0] = "${rss}";
            links[1] = "${playlist}";
            links[2] = "${download}";
            <c:forEach var="jukebox" items="${jukeboxes}" varStatus="loopStatus">
            links[${3 + loopStatus.index}] = "${jukebox.value}";
            </c:forEach>

            displayLink();
        });
    </script>

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

    <textarea id="comment" style="width: 100%">Listening to "${text}"</textarea><br />
    Twitter tweets will have an additional "#MyTunesRSS" at the end. Facebook comments will have a link to
    the MyTunesRSS product page.<br />

    <button>Twitter</button><button>Facebook</button><br />

    <select id="linkSelect" onchange="displayLink()">
        <option>RSS Feed</option>
        <option>Playlist</option>
        <option>Download</option>
        <c:forEach var="jukebox" items="${jukeboxes}">
            <option>${jukebox.key}</option>
        </c:forEach>
    </select><br />
    <div id="linkDisplay"></div>

</div>

</div>

<div class="footer">
    <div class="inner"></div>
</div>

</div>

<jsp:include page="incl_select_flashplayer_dialog.jsp"/>

</body>

</html>