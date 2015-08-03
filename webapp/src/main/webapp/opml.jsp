<?xml version="1.0" encoding="UTF-8"?><%@ page contentType="application/xml; charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>
<opml version="1.1">
    <head>
        <title><c:out value="${title}"/></title>
        <dateCreated>${mtfn:rssDate(creationDate)}</dateCreated>
    </head>
    <body><c:forEach items="${items}" var="item">
        <outline title="${item.name}" text="<c:out value="${item.name}"/>" type="rss" xmlUrl="${item.xmlUrl}" />
    </c:forEach></body>
</opml>
