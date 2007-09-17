<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="refresh" content="0; URL=mytunesrss/<c:if test="${!(param.mytunesrss_com_user eq null)}">?mytunesrss_com_user=${param.mytunesrss_com_user}&amp;mytunesrss_com_cookie=${cwfn:encodeUrl(param.mytunesrss_com_cookie)}</c:if>" />

</head>

<body>

</body>

</html>
