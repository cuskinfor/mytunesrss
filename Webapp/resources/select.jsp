<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

<html>

<head>

    <title>Codewave MyTunesRSS v${cwfn:sysprop('mytunesrss.version')}</title>

    <script type="text/javascript">

        function sort(sortOrder) {
            document.forms["select"].action = "${urlMap.sort}";
            document.forms["select"].elements["sortOrder"].value = sortOrder;
            document.forms["select"].submit();
        }

        function all(ids) {
            var idArray = ids.split(";");
            for (var i = 0; i < idArray.length; i++) {
                document.getElementById("item" + idArray[i]).checked = true;
            }
        }

        function none(ids) {
            var idArray = ids.split(";");
            for (var i = 0; i < idArray.length; i++) {
                document.getElementById("item" + idArray[i]).checked = false;
            }
        }

    </script>

</head>

<body>

<form id="select" action="${urlMap.select}" method="post">

    <input type="hidden" name="sortOrder" value="${sortOrder}" />
    <input type="hidden" name="final" value="true" />

    <jsp:include page="/error.jsp" />

    <b><u>Enter a name for your feed</u></b><br />

    <input type="text" name="channel" value="<c:out value="${param.channel}"/>" /><br /><br />

    <c:if test="${sortOrder != 'Album'}"><a href="#" onclick="sort('Album')">Group by Album</a></c:if>
    <c:if test="${sortOrder != 'Artist'}"><a href="#" onclick="sort('Artist')">Group by Artist</a></c:if>

    <br /><br />

    <table border="0" cellspacing="0" cellpadding="0">
        <c:forEach items="${sections}" var="section">
            <c:set var="commonArtist" value="${section.commonArtist}" />
            <c:set var="commonAlbum" value="${section.commonAlbum}" />
            <tr>
                <td><a href="#" onclick="all('${section.sectionIds}')">all</a>&nbsp;<a href="#" onclick="none('${section.sectionIds}')">none</a></td>
                <td>&nbsp;</td>
                <td>
                    <b><u>
                        <c:choose>
                            <c:when test="${commonArtist && commonAlbum}"><c:out value="${section.firstArtist} - ${section.firstAlbum}" /></c:when>
                            <c:when test="${commonArtist}"><c:out value="${section.firstArtist}" /></c:when>
                            <c:when test="${commonAlbum}"><c:out value="${section.firstAlbum}" /></c:when>
                            <c:otherwise>&nbsp;</c:otherwise>
                        </c:choose>
                    </u></b>
                </td>
            </tr>
            <c:forEach items="${section.items}" var="item">
                <tr>
                    <td align="center"><input type="checkbox" id="item${item.file.id}" name="id" value="${item.file.id}" <c:if test="${item.selected}"> checked="checked"</c:if> /></td>
                    <td>&nbsp;<a href="${servletUrl}/${item.file.id}/${cwfn:urlEncode(item.file.virtualFileName, 'UTF-8')}">play</a>&nbsp;</td>
                    <td>
                        <c:choose>
                            <c:when test="${!commonArtist && !commonAlbum}">
                                <c:out value="${item.file.album} -" />
                                <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                                <c:out value="${item.file.artist}" /> : <span class="title"><c:out value="${item.file.name}" /></span>
                            </c:when>
                            <c:when test="${!commonArtist}">
                                <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                                <c:out value="${item.file.artist}" /> : <span class="title"><c:out value="${item.file.name}" /></span>
                            </c:when>
                            <c:when test="${!commonAlbum}">
                                <c:out value="${item.file.album} -" />
                                <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                                <span class="title"><c:out value="${item.file.name}" /></span>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${!empty item.file.textualTrackNumber}">${item.file.textualTrackNumber} -</c:if>
                                <span class="title"><c:out value="${item.file.name}" /></span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </c:forEach>
    </table>

    <br />

    <input type="submit" value="create feed" />

</form>

</body>

</html>