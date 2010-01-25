<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp" />

</head>

<body class="smartPlaylist">

	<div class="body">
	
		<div class="head">
		    <h1 class="manager">
		        <a class="portal" href="${servletUrl}/showPortal/${auth}"><span><fmt:message key="portal" /></span></a>
		        <span><fmt:message key="myTunesRss" /></span>
		    </h1>
		</div>
	   
		<div class="content">
	   
			<div class="content-inner">
	
			    <jsp:include page="/incl_error.jsp" />
			
			    <form id="playlist" action="${servletUrl}/saveSmartPlaylist/${auth}" method="post">
			
			        <input type="hidden" name="smartPlaylist.playlist.id" value="${smartPlaylist.playlist.id}" />
			
			        <table class="settings" cellspacing="0">
			
			            <tr>
			                <th class="active" colspan="2"><fmt:message key="editSmartPlaylistTitle" /></th>
			            </tr>
			            <mt:initFlipFlop value1="" value2="class=\"odd\""/>
			
			            <tr <mt:flipFlop/>>
			                <td class="label"><fmt:message key="playlistName" /></td>
			                <td><input type="text" name="smartPlaylist.playlist.name" value="<c:out value="${smartPlaylist.playlist.name}"/>" /></td>
			            </tr>
			
			            <tr <mt:flipFlop/>>
			                <td class="label"><fmt:message key="playlistUserPrivate" /></td>
			                <td>
			                    <input type="checkbox"
			                           name="smartPlaylist.playlist.userPrivate"
			                           value="true"
			                           <c:if test="${smartPlaylist.playlist.userPrivate}">checked="checked"</c:if> />
			                </td>
			            </tr>
			
			            <c:forEach items="${fields}" var="field">
			                <tr <mt:flipFlop/>>
			                    <td class="label"><fmt:message key="${field}" /></td>
			                    <c:set var="value" value="${requestScope}"/>
			                    <c:forTokens items="${field}" delims="." var="token">
			                        <c:set var="value" value="${value[token]}"/>
			                    </c:forTokens>
			                    <td><input type="text" name="${field}" value="<c:out value="${value}"/>" /></td>
			                </tr>
			            </c:forEach>
			
			            <tr <mt:flipFlop/>>
			                <td class="label"><fmt:message key="smartPlaylist.smartInfo.protected" /></td>
			                <td>
			                    <select name="smartPlaylist.smartInfo.protected">
			                        <option value=""><fmt:message key="smartPlaylist.smartInfo.protected.null"/></option>
			                        <option value="true" <c:if test="${smartPlaylist.smartInfo.protected == true}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.protected.true"/></option>
			                        <option value="false" <c:if test="${!empty smartPlaylist.smartInfo.protected && smartPlaylist.smartInfo.protected == false}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.protected.false"/></option>
			                    </select>
			                </td>
			            </tr>
			
			            <tr <mt:flipFlop/>>
			                <td class="label"><fmt:message key="smartPlaylist.smartInfo.mediatype" /></td>
			                <td>
			                    <select name="smartPlaylist.smartInfo.mediatype">
			                        <option value=""><fmt:message key="smartPlaylist.smartInfo.mediatype.null"/></option>
			                        <option value="Video" <c:if test="${smartPlaylist.smartInfo.mediaType.jspName == 'Video'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.mediatype.video"/></option>
			                        <option value="Audio" <c:if test="${smartPlaylist.smartInfo.mediaType.jspName == 'Audio'}">selected="selected"</c:if>><fmt:message key="smartPlaylist.smartInfo.mediatype.audio"/></option>
			                    </select>
			                </td>
			            </tr>
			
			        </table>
			
			        <div class="buttons">
			            <input type="submit" value="<fmt:message key="doSave"/>" />
			            <input type="button" value="<fmt:message key="doCancel"/>" onclick="document.location.href='${servletUrl}/showPlaylistManager/${auth}'" />
			        </div>
			
			    </form>

			</div>			    

		</div>
		
		<div class="footer">
			<div class="inner"></div>
		</div>
	
	</div>

</body>

</html>