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

    <script src="http://platform.twitter.com/widgets.js" type="text/javascript"></script>

    <script type="text/javascript">

        var links = new Array();
        var facebookMessage = '${mtfn:escapeJs(facebookText)}';

        function displayLink() {
            var selectedLink = links[$jQ("#linkSelect option:selected")[0].index];
            $jQ("#linkDisplay").text(selectedLink);
            for (var i = 0; i < links.length; i++) {
                $jQ("#twitterButton" + i).css("display", "none");
            }
            $jQ("#twitterButton" + $jQ("#linkSelect option:selected")[0].index).css("display", "block");
            $jQ("#facebookMessage").val(facebookMessage + "\r\n\r\n" + selectedLink);
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
        <li class="back">
            <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
        </li>
    </ul>

    <select id="linkSelect" onchange="displayLink()">
        <option><fmt:message key="tooltip.rssfeed"/></option>
        <option><fmt:message key="tooltip.playlist"/></option>
        <option><fmt:message key="tooltip.playtrack"/></option>
        <c:forEach var="jukebox" items="${jukeboxes}">
            <option>${jukebox.key}</option>
        </c:forEach>
    </select><br />

    <div id="linkDisplay"></div>

    <div id="twitterButton0">
        <div>
            <a href="http://twitter.com/share" class="twitter-share-button" data-text="<c:out value="${twitterText}"/>"
               data-related="mytunesrss:MyTunesRSS Media Server" data-url="${rss}" data-count="none">Tweet</a>
        </div>
    </div>
    <div id="twitterButton1" style="display:none">
        <div>
            <a href="http://twitter.com/share" class="twitter-share-button" data-text="<c:out value="${twitterText}"/>"
               data-related="mytunesrss:MyTunesRSS Media Server" data-url="${playlist}" data-count="none">Tweet</a>
        </div>
    </div>
    <div id="twitterButton2" style="display:none">
        <div>
            <a href="http://twitter.com/share" class="twitter-share-button" data-text="<c:out value="${twitterText}"/>"
               data-related="mytunesrss:MyTunesRSS Media Server" data-url="${download}" data-count="none">Tweet</a>
        </div>
    </div>
    <c:forEach var="jukebox" items="${jukeboxes}" varStatus="loopStatus">
        <div id="twitterButton${3 + loopStatus.index}" style="display:none">
            <div>
                <a href="http://twitter.com/share" class="twitter-share-button" data-text="<c:out value="${twitterText}"/>"
                   data-related="mytunesrss:MyTunesRSS Media Server" data-url="${jukebox.value}" data-count="none">Tweet</a>
            </div>
        </div>
    </c:forEach>

    <form id="facebookForm" action="http://www.facebook.com/dialog/feed" method="post">
        <input type="hidden" name="app_id" value="102138059883364"/>
        <input type="hidden" name="link" value="http://www.codewave.de/products/mytunesrss"/>
        <input type="hidden" name="picture" value="http://mytunesrss.com/mytunesrss_fb.png"/>
        <input type="hidden" name="name" value="MyTunesRSS"/>
        <input type="hidden" name="caption" value="Your personal Media Server"/>
        <input type="hidden" name="description" value="Enjoy your music, movies and photos from anywhere in the world. All you need is a web browser and internet access."/>
        <input id="facebookMessage" type="hidden" name="message" value=""/>
        <input type="hidden" name="redirect_uri" value="${mtfn:decode64(param.backUrl)}"/>
    </form>

    <div>
        <button onclick="$jQ('#facebookForm').submit()">Facebook</button>
    </div>

</div>

</div>

<div class="footer">
    <div class="inner"></div>
</div>

</div>

<jsp:include page="incl_select_flashplayer_dialog.jsp"/>

</body>

</html>