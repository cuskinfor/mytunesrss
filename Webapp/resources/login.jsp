<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="title" /> v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
    <![endif]-->
</head>

<body>

<div class="body">

    <h1 class="search"><span>MyTunesRSS</span></h1>

    <jsp:include page="/error.jsp" />

    <form id="login" action="${servletUrl}/login" method="post">

        <table class="search" cellspacing="0">
            <tr>
                <th colspan="5"><fmt:message key="login.caption" /></th>
            </tr>
            <tr>
                <td class="spacer">&nbsp;</td>
                <td class="artist"><fmt:message key="login.password" /></td>
                <td class="input"><input class="text" type="password" name="password" value="<c:out value="${param.password}"/>" /></td>
                <td class="button"><input class="button" type="submit" value="<fmt:message key="login.login"/>" /></td>
                <td class="spacer">&nbsp;</td>
            </tr>
        </table>

    </form>

</div>

</body>

</html>
