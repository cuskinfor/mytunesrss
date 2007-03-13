<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title>
        <fmt:message key="applicationTitle" />
        v${mytunesrssVersion}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css?ts=${sessionCreationTime}" />
    <!--[if IE]>
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css?ts=${sessionCreationTime}" />
  <![endif]-->

</head>

<body>

<div class="body">

    <h1 class="manager">
        <a class="portal" href="${servletUrl}/showPortal"><fmt:message key="portal"/></a> <span><fmt:message key="myTunesRss"/></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <table cellspacing="0">
        <tr>
            <th class="active"><fmt:message key="fileUpload"/></th>
        </tr>
        <tr>
            <td><fmt:message key="fileUploadInfo"/></td>
        </tr>
        <tr class="odd">
            <td>
                <form name="upload" enctype="multipart/form-data" method="post" action="${servletUrl}/upload">
                    <input type="file" name="file" /> <input type="submit" value="<fmt:message key="doUpload"/>" />
                </form>
            </td>
        </tr>
    </table>

</div>

</body>
</html>
