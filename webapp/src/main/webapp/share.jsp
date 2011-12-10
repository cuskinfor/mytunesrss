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

<script type="text/javascript">

    var links = new Array();

    function displayLink() {
        var selectedLink = links[$jQ("#linkSelect option:selected")[0].index];
        $jQ("#linkDisplay").text(selectedLink);
        for (var i = 0; i < links.length; i++) {
            $jQ("#twitterButton" + i).css("display", "none");
        }
        $jQ("#twitterButton" + $jQ("#linkSelect option:selected")[0].index).css("display", "block");
    }

    $jQ(document).ready(function() {
        var idx = 0;
        <c:if test="${!empty rss}">links[idx++] = "${rss}";</c:if>
        <c:if test="${!empty playlist}">links[idx++] = "${playlist}";</c:if>
        <c:if test="${!empty download}">links[idx++] = "${download}";</c:if>
        <c:forEach var="jukebox" items="${jukeboxes}" varStatus="loopStatus">
        links[idx++] = "${jukebox.value}";
        </c:forEach>
        displayLink();
    });

    function shareTwitter() {
        $jQ("#twitterMessage").val("${mtfn:escapeJs(twitterText)}");
        $jQ("#twitterUrl").val(links[$jQ("#linkSelect option:selected")[0].index]);
        centerPopupWindow("", "MyTunesRssTwitter", 550, 450, "resizable=no,location=no,menubar=no,scrollbars=no,status=no,toolbar=no,hotkeys=no");
        $jQ('#twitterForm').submit();
        $jQ.modal.close();
    }

    function shareFacebook() {
        showLoading("<fmt:message key="facebook.shorteningUrl"/>");
        new $jQ.ajax({
            url : "${servletUrl}/shortenUrl/${auth}",
            type : "POST",
            contentType : "application/x-www-form-urlencoded",
            processData : true,
            data : {
                "url" : links[$jQ("#linkSelect option:selected")[0].index]
            },
            success : function(data) {
                $jQ("#facebookLink").val(data);
                hideLoading();
                centerPopupWindow("", "MyTunesRssFacebook", 980, 480, "resizable=yes,location=no,menubar=no,scrollbars=auto,status=no,toolbar=no,hotkeys=no");
                $jQ('#facebookForm').submit();
                $jQ.modal.close();
            },
            error : function() {
                $jQ("#facebookLink").val(links[$jQ("#linkSelect option:selected")[0].index]);
                hideLoading();
                centerPopupWindow("", "MyTunesRssFacebook", 980, 480, "resizable=yes,location=no,menubar=no,scrollbars=auto,status=no,toolbar=no,hotkeys=no");
                $jQ('#facebookForm').submit();
                $jQ.modal.close();
            }
        });
    }

</script>

<fmt:message key="share.selectLink"/>:
<select id="linkSelect" onchange="displayLink()">
    <c:if test="${!empty rss}"><option><fmt:message key="tooltip.rssfeed"/></option></c:if>
    <c:if test="${!empty playlist}"><option><fmt:message key="tooltip.playlist"/></option></c:if>
    <c:if test="${!empty download}"><option><fmt:message key="tooltip.playtrack"/></option></c:if>
    <c:forEach var="jukebox" items="${jukeboxes}">
        <option>${jukebox.key}</option>
    </c:forEach>
</select>

<div class="linkDisplay" id="linkDisplay"></div>

<button id="linkTwitter" onclick="shareTwitter()">Twitter</button>
<c:if test="${!empty globalConfig.facebookApiKey}">
    <button id="linkFacebook" onclick="shareFacebook()">Facebook</button>
</c:if>

<form id="twitterForm" action="http://twitter.com/share" method="post" target="MyTunesRssTwitter">
    <input type="hidden" id="twitterUrl" name="url" value=""/>
    <input type="hidden" id="twitterMessage" name="text" value=""/>
    <input type="hidden" name="related" value="mytunesrss:MyTunesRSS Media Server"/>
</form>
<c:if test="${!empty globalConfig.facebookApiKey}">
    <form id="facebookForm" action="http://www.facebook.com/dialog/feed" method="post" target="MyTunesRssFacebook">
        <input type="hidden" name="app_id" value="${globalConfig.facebookApiKey}"/>
        <input id="facebookLink" type="hidden" name="link" value="http://www.codewave.de/products/mytunesrss"/>
        <input type="hidden" name="picture" value="http://mytunesrss.com/mytunesrss_fb.png"/>
        <input type="hidden" name="name" value="<c:out value="${artistAndTitle}"/>"/>
        <input type="hidden" name="caption" value="MyTunesRSS - Your personal Media Server"/>
        <input type="hidden" name="description"
               value="Enjoy your music, movies and photos from anywhere in the world. All you need is a web browser and internet access."/>
        <input type="hidden" name="redirect_uri" value="http://mytunesrss.com/tools/close_window.php"/>
    </form>
</c:if>
