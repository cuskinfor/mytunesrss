<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="mttag" %>

<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        function loadPhoto() {
            var width = $jQ("div.photoback").width() - $jQ("#photoimage").css("padding-left").replace("px", "") - $jQ("#photoimage").css("padding-right").replace("px", "");
            $jQ("#photoimage").attr("src", "${mtfn:photoLink(pageContext, photos[param.photoIndex], null)}" + "/size=" + width + "/jpegQuality=${config.photoJpegQuality}");
        }

        function showExif(exifUrl) {
            showLoading("loading...");
            new $jQ.ajax({
                url : exifUrl,
                type : "GET",
                processData : false,
                success : function(data) {
                    hideLoading();
                    $jQ("#exifData").empty();
                    if (data.length > 0) {
                        $jQ("#exifData").append("<table>");
                        $jQ.each(data, function(index, field) {
                            $jQ("#exifData").append("<tr><td align=\"right\">" + field.name + ":</td><td id=\"value_" + field.name.replace(/[^a-zA-Z0-9]/g, "_") + "\" align=\"left\">" + field.value + "</td></tr>");
                        });
                        $jQ("#exifData").append("<table>");
                    } else {
                        $jQ("#exifData").append("<p><fmt:message key="noExifData"/></p>");
                    }
                    openDialog("#displayExifDialog");
                }
            });
        }
    </script>

</head>

<body class="browse" onload="loadPhoto()">

<div class="body">

    <div class="head">
        <h1 class="browse">
            <span><fmt:message key="myTunesRss"/></span>
        </h1>
    </div>

    <div class="content">

        <div class="content-inner">

            <ul class="menu">
                <li class="back">
                    <a id="linkBack" href="${servletUrl}/browsePhoto/${auth}/<mt:encrypt key="${encryptionKey}">/photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/index=${photoPage}</mt:encrypt>/backUrl=${param.photosBackUrl}"><fmt:message key="back"/></a>
                </li>
            </ul>

            <jsp:include page="/incl_error.jsp" />

            <table cellspacing="0" class="tracklist searchResult">
                <tr>
                    <th class="active">
                        <span><c:out value="${mtfn:decode64(param.photoalbum)}"/></span>
                    </th>
                </tr>
            </table>

            <div class="photoback">
                <img id="photoimage" class="singlephoto" src="${themeUrl}/images/animated_progress.gif" />
                <c:if test="${param.photoIndex > 0}">
                    <div id="leftphotobutton" class="leftphotobutton" onclick="self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${param.photoIndex - 1}</mt:encrypt>/photosBackUrl=${param.photosBackUrl}/size=' + $jQ('div.content-inner').innerWidth()"></div>
                </c:if>
                <c:if test="${param.photoIndex + 1 lt fn:length(photos)}">
                    <div id="rightphotobutton" class="rightphotobutton" onclick="self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${param.photoIndex + 1}</mt:encrypt>/photosBackUrl=${param.photosBackUrl}/size=' + $jQ('div.content-inner').innerWidth()"></div>
                </c:if>
            </div>

            <div class="photolinks">
                <a id="downfullphotolink" href="${servletUrl}/downloadPhoto/${auth}/<mt:encrypt key="${encryptionKey}">photo=${cwfn:encodeUrl(photos[param.photoIndex].id)}</mt:encrypt>"><img src="${themeUrl}/images/action-download.png"><span><fmt:message key="downloadFullSizedPhoto"/></span></a><br/>
                <a id="exiflink" onclick="showExif('${servletUrl}/showExif/${auth}/<mt:encrypt key="${encryptionKey}">photo=${cwfn:encodeUrl(photos[param.photoIndex].id)}</mt:encrypt>')"><img src="${themeUrl}/images/action-tags.png"><span><fmt:message key="showExifData"/></span></a>
            </div>

        </div>

    </div>

    <div class="footer">
        <div class="inner"></div>
    </div>

</div>

<div id="displayExifDialog" class="dialog">
    <h2>
        <fmt:message key="displayExifDialogTitle"/>
    </h2>
    <div>
        <p id="exifData"></p>
        <p align="right">
            <button id="close_exif" onclick="$jQ.modal.close()"><fmt:message key="doClose"/></button>
        </p>
    </div>
</div>

</body>

</html>