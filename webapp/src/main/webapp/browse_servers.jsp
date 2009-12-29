<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<c:set var="backUrl" scope="request">${servletUrl}/browseAlbum/${auth}/<mt:encrypt key="${encryptionKey}">artist=${cwfn:encodeUrl(param.artist)}/page=${param.page}/index=${param.index}</mt:encrypt></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body class="browse">

    <div class="body">
    
        <div class="head">        
            <h1 class="serverbrowser">
                <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal"/></span></a>
                <span><fmt:message key="myTunesRss"/></span>
            </h1>
        </div>
        
        <div class="content">
        
            <div class="content-inner">
    
                <jsp:include page="/incl_error.jsp" />
                
                <jsp:include page="incl_playlist.jsp" />
                
                <form id="browse" action="" method="post">
                
                	<fieldset>
                        <input type="hidden" name="backUrl" value="${mtfn:encode64(backUrl)}" />
                	</fieldset>
                
                    <table class="tracklist" cellspacing="0">
                        <tr>
                            <th class="active">
                                <fmt:message key="serverName"/>
                            </th>
                            <th><fmt:message key="serverAddress"/></th>
                        </tr>
                        <c:forEach items="${servers}" var="server" varStatus="loopStatus">
                            <tr class="${cwfn:choose(loopStatus.index % 2 == 0, 'even', 'odd')}">
                                <td>
                                    <a href="http://${server.address}:${server.port}"><c:out value="${server.name}" /></a>
                                </td>
                                <td>
                                    <a href="http://${server.address}:${server.port}">${server.address}</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                
                    <c:if test="${!empty indexPager}">
                        <c:set var="pager" scope="request" value="${indexPager}" />
                        <c:set var="pagerCommand" scope="request">${servletUrl}/browseServers/${auth}/<mt:encrypt key="${encryptionKey}">page=${param.page}</mt:encrypt>/index={index}</c:set>
                        <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
                        <jsp:include page="incl_bottomPager.jsp" />
                    </c:if>
                
                </form>
                
            </div>
            
        </div>
        
        <div class="footer">
            <div class="footer-inner"></div>
        </div>
    
    </div>

</body>

</html>
