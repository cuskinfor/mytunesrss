<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<c:set var="backUrl" scope="request">${servletUrl}/browseTrack?playlist=${param.playlist}&amp;fullAlbums=${param.fullAlbums}&amp;album=${cwfn:urlEncode(param.album, 'UTF-8')}&amp;artist=${cwfn:urlEncode(param.artist, 'UTF-8')}&amp;searchTerm=${cwfn:urlEncode(param.searchTerm, 'UTF-8')}&amp;index=${param.index}&amp;backUrl=${cwfn:urlEncode(param.backUrl, 'UTF-8')}&amp;sortOrder=${sortOrder}</c:set>

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="applicationTitle" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
    <![endif]-->
		<script src="${appUrl}/js/functions.js" type="text/javascript"></script>

</head>

<body>

<div class="body">

<h1 class="info">
    <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
</h1>

<jsp:include page="/incl_error.jsp" />

<table cellspacing="0">
	<tr>
		<th colspan="2" class="active">Track Titel</th>
	</tr>
	<tr>
		<td>Artist:</td>
		<td>Madonna</td>
	</tr>
	<tr class="odd">
		<td>Title:</td>
		<td>Music</td>
	</tr>
	<tr>
		<td>Album:</td>
		<td>Music</td>
	</tr>
	<tr class="odd">
		<td>Dauer:</td>
		<td>10 Min.</td>
	</tr>
	<tr>
		<td>Type:</td>
		<td>Mp3 ...</td>
	</tr>
	<tr class="odd">
		<td>Download:</td>
		<td><img src="${appUrl}/images/download_odd.gif" style="vertical-align:middle; margin-right:5px;"/><a href="#">Madonna - Music.mp3</a></td>
	</tr>
</table>

</div>

</body>

</html>