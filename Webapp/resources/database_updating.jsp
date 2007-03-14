<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

    <meta http-equiv="refresh" content="5; URL=${servletUrl}/showPortal" />

</head>

<div class="body">

    <h1 class="search"><span><fmt:message key="myTunesRss"/></span></h1>

    <form id="login" action="${servletUrl}/login" method="post">

        <h2 class="fatalError"><fmt:message key="info.databaseUpdating.title"/></h2>

				<div class="fatalError">

					<fmt:message key="info.databaseUpdating.message"/>

				</div>

    </form>

</div>

</body>

</html>