<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <c:if test="${uploadPercentage < 100}"><meta http-equiv="refresh" content="2; URL=" /></c:if>
</head>

<body onload="parent.updateProgress()">
    <div id="progress">${uploadPercentage}</div>
</body>

</html>