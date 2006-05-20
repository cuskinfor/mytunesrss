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
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
		<link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
	<![endif]-->

</head>

<body onLoad="document.forms[0].elements['password'].focus()">

<div class="body">

    <h1 class="search"><span>MyTunesRSS</span></h1>

    <jsp:include page="/incl_error.jsp" />

    <form id="login" action="${servletUrl}/login" method="post">

        <h2><fmt:message key="login.caption" /></h2>
				
				<div class="login">

					<table class="login" cellspacing="0">
						<tr>
							<td><fmt:message key="login.password" /></td>
							<td>
								<input class="text" type="password" name="password" value="<c:out value="${param.password}"/>" />
							</td>
							<td>
								<input class="button" type="submit" value="<fmt:message key="login.login"/>" />
							</td>
						</tr>
						<tr>
							<td>&nbsp;</td>
							<td>
								<input type="checkbox" name="rememberLogin" value="true" />
								Remember login
							</td>
							<td>&nbsp;</td>
						</tr>
					</table>
					
				</div>

    </form>

</div>

</body>

</html>