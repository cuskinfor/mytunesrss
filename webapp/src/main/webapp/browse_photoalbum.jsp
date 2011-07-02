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
<%--@elvariable id="photoAlbums" type="java.util.List<de.codewave.mytunesrss.datastore.statement.PhotoAlbum>"--%>

<c:set var="backUrl" scope="request">${servletUrl}/browsePhotoAlbum/${auth}/<mt:encrypt key="${encryptionKey}">index=${param.index}</mt:encrypt>/backUrl=${param.backUrl}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

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
                    <%--td class="actions">
                        <mttag:actions index="${fnCount}"
                                       backUrl="${mtfn:encode64(backUrl)}"
                                       linkFragment="photoalbum=${mtfn:encode64(photoAlbum)}"
                                       filename="${mtfn:webSafeFileName(photoAlbum)}"
                                       defaultPlaylistName="${photoAlbum}"
                                       shareText="${photoAlbum}" />
                    </td--%>
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

</body>

</html>