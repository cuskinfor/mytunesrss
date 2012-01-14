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
        function resize() {
            var newSize = $jQ("#photoSizeSelector").val();
            self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${param.photoIndex}</mt:encrypt>/photosBackUrl=${param.photosBackUrl}/size=' + newSize;
        }
        function setMaxWidth() {
            $jQ("#photoimage").css("max-width", ($jQ("div.photoback").width() - $jQ("#photoimage").css("padding-left").replace("px", "") - $jQ("#photoimage").css("padding-right").replace("px", "")) + "px");
            //$jQ("#photoimage").css("max-width", ($jQ("div.photoback").width() - (2 * $jQ("#photoimage").css("padding"))) + "px");
        }
    </script>

</head>

<body class="browse" onload="setMaxWidth()">

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
                        <span style="float:right">
                            <fmt:message key="photos.size" />:
                            <select id="photoSizeSelector" name="photoSize" onchange="resize()">
                                <option value="25"><fmt:message key="photos.size.25" /></option>
                                <option <c:if test="${config.photoSize == 50}">selected="selected"</c:if> value="50"><fmt:message key="photos.size.50" /></option>
                                <option <c:if test="${config.photoSize == 75}">selected="selected"</c:if> value="75"><fmt:message key="photos.size.75" /></option>
                                <option <c:if test="${config.photoSize == 100}">selected="selected"</c:if> value="100"><fmt:message key="photos.size.100" /></option>
                            </select>
                        </span>
                    </th>
                </tr>
            </table>

            <c:set var="sizeParam">size=${config.photoSize}</c:set>
            <div class="photoback">
                <img id="photoimage" class="singlephoto" src="${mtfn:photoLink(pageContext, photos[param.photoIndex], sizeParam)}" />
                <c:if test="${param.photoIndex > 0}">
                    <div class="leftphotobutton" onclick="self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${param.photoIndex - 1}</mt:encrypt>/photosBackUrl=${param.photosBackUrl}'"></div>
                </c:if>
                <c:if test="${param.photoIndex + 1 lt fn:length(photos)}">
                    <div class="rightphotobutton" onclick="self.document.location.href='${servletUrl}/browseSinglePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}/photoalbumid=${param.photoalbumid}/photoIndex=${param.photoIndex + 1}</mt:encrypt>/photosBackUrl=${param.photosBackUrl}'"></div>
                </c:if>
            </div>

            <div>
                <a href="${servletUrl}/downloadPhoto/${auth}/<mt:encrypt key="${encryptionKey}">photo=${photos[param.photoIndex].id}</mt:encrypt>">Download full sized photo</a>
            </div>

        </div>

    </div>

    <div class="footer">
        <div class="inner"></div>
    </div>

</div>

</body>

</html>