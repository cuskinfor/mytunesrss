<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<html>
<head>
    <title>Codewave MyTunesRSS Feed</title>
    <script type="text/javascript">

        function sort(sortMethod) {
            document.forms['select'].elements['method'].value = sortMethod;
            document.forms['select'].submit();
        }

    </script>
</head>

<body>
<form id="select" action="${servletUrl}" method="get">
    <input type="hidden" name="method" value="getRssFeed" />

    <jsp:include page="/error.jsp" />

    <h1>Select results for your personal MyTunesRSS feed</h1>

    <h2>Enter a name for your feed</h2>
    <input type="text" name="channel" value="<c:out value="${param.channel}"/>" />

    <h2>Select titles for your feed</h2>

    <a href="#" onclick="sort('sortResultsByAlbum')">Group by Album</a> <a href="#" onclick="sort('sortResultsByArtist')">Group by Artist</a>

    <c:forEach items="${sections}" var="section">
        <c:set var="commonArtist" value="${section.commonArtist}" />
        <c:set var="commonAlbum" value="${section.commonAlbum}" />
        <h3>
            <c:choose>
                <c:when test="${commonArtist && commonAlbum}"><c:out value="${section.firstArtist} - ${section.firstAlbum}" /></c:when>
                <c:when test="${commonArtist}"><c:out value="${section.firstArtist}" /></c:when>
                <c:when test="${commonAlbum}"><c:out value="${section.firstAlbum}" /></c:when>
                <c:otherwise>&nbsp;</c:otherwise>
            </c:choose>
        </h3>
        <c:forEach items="${section.items}" var="item">
            <input type="checkbox" name="id" value="${item.file.id}" <c:if test="${item.selected}"> checked="checked"</c:if> />&nbsp;
            <a href="${servletUrl}/${item.file.id}/${cwfn:urlEncode(item.file.virtualFileName, 'UTF-8')}">play</a>&nbsp;
                                                                                                                  <c:choose>
                                                                                                                      <c:when test="${!commonArtist && !commonAlbum}">
                                                                                                                          <c:out value="${item.file.album} -" />
                                                                                                                          <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber}
                                                                                                                              -</c:if>
                                                                                                                          <c:out value="${item.file.artist}" />
                                                                                                                          : <span class="title"><c:out
                                                                                                                              value="${item.file.name}" /></span>
                                                                                                                      </c:when>
                                                                                                                      <c:when test="${!commonArtist}">
                                                                                                                          <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber}
                                                                                                                              -</c:if>
                                                                                                                          <c:out value="${item.file.artist}" />
                                                                                                                          : <span class="title"><c:out
                                                                                                                              value="${item.file.name}" /></span>
                                                                                                                      </c:when>
                                                                                                                      <c:when test="${!commonAlbum}">
                                                                                                                          <c:out value="${item.file.album} -" />
                                                                                                                          <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber}
                                                                                                                              -</c:if>
                                                                                                                          <span class="title"><c:out
                                                                                                                                  value="${item.file.name}" /></span>
                                                                                                                      </c:when>
                                                                                                                      <c:otherwise>
                                                                                                                          <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber}
                                                                                                                              -</c:if>
                                                                                                                          <span class="title"><c:out
                                                                                                                                  value="${item.file.name}" /></span>
                                                                                                                      </c:otherwise>
                                                                                                                  </c:choose><br />
        </c:forEach>
    </c:forEach>
    <a href="#" onclick="document.forms[0].submit()">create feed</a>
</form>
</body>
</html>