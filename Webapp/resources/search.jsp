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

			<h1 class="search"><span>MyTunesRSS</span></h1>

      <jsp:include page="/error.jsp" />

			<table class="search" cellspacing="0">
                <form id="search" action="${urlMap.search}" method="post">
				<tr>
					<th colspan="5"><fmt:message key="search.caption"/></th>
				</tr>
				<tr>
					<td class="spacer" rowspan="2">&nbsp;</td>
					<td class="artist"><fmt:message key="artist"/></td>
					<td class="input" colspan="2"><input class="text" type="text" name="artist" value="<c:out value="${param.artist}"/>" /></td>
					<td class="spacer" rowspan="2">&nbsp;</td>
				</tr>
				<tr>
					<td class="artist"><fmt:message key="album"/></td>
					<td class="input"><input class="text" type="text" name="album" value="<c:out value="${param.album}"/>" /></td>
					<td class="button"><input class="button" type="submit" value="<fmt:message key="search.search"/>" /></td>
				</tr>
                </form>
				<c:if test="${!empty playlists}">

					<tr>
						<th colspan="5"><fmt:message key="playlist.caption"/></th>
					</tr>
					<tr>
						<td class="spacer">&nbsp;</td>
						<td class="artist">Playlists</td>
                        <form id="playlist" action="${urlMap.playlist}" method="post">
						<td class="input">
							<select class="text" name="playlist">
								<c:forEach items="${playlists}" var="playlist">
									<option value="${playlist.id}">${playlist.name}</option>
								</c:forEach>
							</select>
						</td>
						<td class="button">
							<input class="button" type="submit" value="<fmt:message key="playlist.use"/>" />
						</td>
                        </form>
						<td class="spacer">&nbsp;</td>
					</tr>

				</c:if>
			</table>

    </div>

  </body>

</html>
