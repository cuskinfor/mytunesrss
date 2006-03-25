<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<html>
<head>
    <title>Codewave MyTunesRSS v${cwfn:sysprop('mytunesrss.version')}</title>
</head>

<body>
<form id="search" action="${servletUrl}" method="post">
    <input type="hidden" name="method" value="executeSearch" />
    <jsp:include page="/error.jsp" />
    <h1>Search the iTunes Music Library</h1>
    Album <input type="text" name="album" value="<c:out value="${param.album}"/>" /><br />
    Artist <input type="text" name="artist" value="<c:out value="${param.artist}"/>" /><br />
    <a href="#" onclick="document.forms[0].submit()">search music</a>
</form>
</body>
</html>