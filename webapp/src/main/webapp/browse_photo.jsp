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
<%--@elvariable id="authUser" type="de.codewave.mytunesrss.User"--%>
<%--@elvariable id="globalConfig" type="de.codewave.mytunesrss.MyTunesRssConfig"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>
<%--@elvariable id="photos" type="java.util.List<de.codewave.mytunesrss.datastore.statement.Track>"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browsePhotoAlbum/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <script type="text/javascript">
        $jQ(document).ready(function() {
            $jQ("img").fullsize();
        });
    </script>

    <style type="text/css">
        .wraptocenter {
            text-align: center;
            vertical-align: middle;
        }

        .wraptocenter * {
            vertical-align: middle;
        }

            /*\*//*/
            .wraptocenter {
                display: block;
            }
            .wraptocenter span {
                display: inline-block;
                height: 100%;
                width: 1px;
            }
            /**/
    </style>
    <!--[if lt IE 8]><style>
    .wraptocenter span {
        display: inline-block;
        height: 100%;
    }
</style><![endif]-->

</head>

<body class="browse">

    <div class="body">
    
        <div class="head">    
            <h1 class="browse">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>
        
        <div class="content">
            
            <div class="content-inner">
                
                <ul class="menu">
                    <li class="back">
                        <a href="${mtfn:decode64(param.backUrl)}"><fmt:message key="back"/></a>
                    </li>
                </ul>
                
                <jsp:include page="/incl_error.jsp" />
                
                <table cellspacing="0" class="tracklist searchResult">
                    <c:set var="fnCount" value="0"/>
                    <c:forEach items="${photos}" var="photo" varStatus="loopStatus">
                        <c:if test="${fnCount % 4 == 0}"><tr></c:if>
                        <td class="wraptocenter"><span></span><img src="${servletUrl}/showImage/${auth}/<mt:encrypt key="${encryptionKey}">hash=${photo.imageHash}/size=128</mt:encrypt>" longdesc="${mtfn:playbackLink(pageContext, photo, '')}"/></td>
                        <c:if test="${fnCount % 4 == 3 || loopStatus.last}"></tr></c:if>
                    <c:set var="fnCount" value="${fnCount + 1}"/>
                    </c:forEach>
                </table>
                
                <c:if test="${!empty pager}">
                    <c:set var="pagerCommand"
                           scope="request">${servletUrl}/browsePhoto/${auth}/<mt:encrypt key="${encryptionKey}">photoalbum=${param.photoalbum}</mt:encrypt>/index={index}/backUrl=${param.backUrl}</c:set>
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

</body>

</html>