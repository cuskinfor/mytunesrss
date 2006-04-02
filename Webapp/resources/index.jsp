<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>

<html>

<head>
    <title><fmt:message key="title"/> v${cwfn:sysprop('mytunesrss.version')}</title>
</head>

<body>

<jsp:include page="/error.jsp" />

<b><u><fmt:message key="index.caption"/></u></b><br /><br />

<form id="search" action="${urlMap.search}" method="post">

    <table border="0" cellspacing="0" cellpadding="1">
        <tr><td><fmt:message key="album"/></td><td><input type="text" name="album" value="<c:out value="${param.album}"/>" /></td></tr>
        <tr><td><fmt:message key="artist"/></td><td><input type="text" name="artist" value="<c:out value="${param.artist}"/>" /></td></tr>
        <tr><td>&nbsp;</td><td><br /><input type="submit" value="<fmt:message key="index.search"/>" /></td></tr>
    </table>

</form>

</body>

</html>
