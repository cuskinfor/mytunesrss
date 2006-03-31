<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<html>

<head>
    <title>Codewave MyTunesRSS v${cwfn:sysprop('mytunesrss.version')}</title>
</head>

<body>

<jsp:include page="/error.jsp" />

<b><u>Search the iTunes Music Library</u></b><br /><br />

<form id="search" action="${urlMap.search}" method="post">

    <table border="0" cellspacing="0" cellpadding="1">
        <tr><td>Album</td><td><input type="text" name="album" value="<c:out value="${param.album}"/>" /></td></tr>
        <tr><td>Artist</td><td><input type="text" name="artist" value="<c:out value="${param.artist}"/>" /></td></tr>
        <tr><td>&nbsp;</td><td><br /><input type="submit" value="search music" /></td></tr>
    </table>

</form>

</body>

</html>
