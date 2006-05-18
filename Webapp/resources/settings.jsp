<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<c:set var="backUrl" scope="request">${servletUrl}/showPlaylistManager</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="title" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
  <![endif]-->

</head>

<body>

<div class="body">

    <h1 class="settings">
      <a class="portal" href="${servletUrl}/showPortal">Portal</a>
			<span>MyTunesRSS</span>
		</h1>

    <jsp:include page="/incl_error.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active">Settings</th>
            <th>&nbsp;</th>
        </tr>
        <tr>
					<td>Pseudo MP3 suffix</td>
					<td><input type="text" value=""/></td>
        </tr>
        <tr class="odd">
					<td>Pseudo M4A suffix</td>
					<td><input type="text" value=""/></td>
        </tr>
        <tr>
					<td>Einträge pro Feed begrenzen</td>
					<td>
						<input type="checkbox" value=""/>
						auf
						<input type="text" style="width: 50px;"/>
					</td>
        </tr>
        <tr class="odd">
					<td>Playlist Typen</td>
					<td>
						<input type="checkbox" value=""/> RSS
						<input type="checkbox" value="" style="margin-left: 15px;"/> M3U
					</td>
        </tr>
        <tr>
					<td>Login-Cookie setzen</td>
					<td><input type="checkbox" value=""/></td>
        </tr>
    </table>
		
		<div class="buttons">
			<input type="button" value="save"/>
		</div>

</div>

</body>

</html>
