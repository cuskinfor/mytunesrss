<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

  <head>

    <title><fmt:message key="title"/> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="styles/mytunesrss.css" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="styles/ie.css" />
    <![endif]-->

  </head>

  <body>

    <div class="body">

			<h1 class="index">MyTunesRSS</h1>

      <jsp:include page="/error.jsp" />


				<table class="search" cellspacing="0">
					<tr>
						<th style="width: 50%;">&nbsp;</th>
						<th <c:if test="${!empty playlists}">colspan="2"</c:if>><fmt:message key="search.caption"/></th>
						<th style="width: 50%;">&nbsp;</th>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td>
							<form id="search" action="${urlMap.search}" method="post">
								<fmt:message key="album"/><br/>
								<input class="text" type="text" name="album" value="<c:out value="${param.album}"/>" /><br/>
								<fmt:message key="artist"/><br/>
								<input class="text" type="text" name="artist" value="<c:out value="${param.artist}"/>" /><br/>
								<input class="button" type="submit" value="<fmt:message key="search.search"/>" />
							</form>
						</td>
						<c:if test="${!empty playlists}">
							<td class="playlist">
								<div class="playlist">
									<form id="playlist" action="${urlMap.playlist}" method="post">
										<fmt:message key="playlist.caption"/><br/>
										<select class="text" name="playlist">
											<c:forEach items="${playlists}" var="playlist">
												<option value="${playlist.id}">${playlist.name}</option>
											</c:forEach>
										</select>
										<input class="button" type="submit" value="<fmt:message key="playlist.use"/>" />
									</form>
								</div>
							</td>
						</c:if>
						<td>&nbsp;</td>
					</tr>
				</table>

    </div>

  </body>

</html>
