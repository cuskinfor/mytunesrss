<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

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

    <script type="text/javascript">

        function sort(sortOrder) {
            document.forms["browse"].action = "${servletUrl}/browseTrack";
            document.forms["browse"].elements["sortOrder"].value = sortOrder;
            document.forms["browse"].submit();
        }

        function selectAll(ids, checkbox) {
            var idArray = ids.split(";");
            for (var i = 0; i < idArray.length; i++) {
                document.getElementById("item" + idArray[i]).checked = checkbox.checked == true ? true : false;
            }
        }

        function registerTR() {
            var trs = document.getElementsByTagName("TR");
            for (var i = 0; i < trs.length; i++) {
                if (trs[i].getElementsByTagName("TH").length > 0) {
                    trs[i].getElementsByTagName("TH")[1].onclick = function() {
                        this.parentNode.getElementsByTagName("INPUT")[0].click()
                    };
                }
                if (trs[i].getElementsByTagName("TH").length == 0) {
                    trs[i].getElementsByTagName("TD")[1].onclick = selectTrack;
                }
            }
        }

        function selectTrack() {
            var checkbox = this.parentNode.getElementsByTagName("input")[0];
            checkbox.checked = ( checkbox.checked == true ) ? false : true;
        }

    </script>

</head>

<body onLoad="registerTR()">

<div class="body">

    <h1 class="searchResult"><span>MyTunesRSS</span></h1>

    <jsp:include page="/error.jsp" />

    <ul class="links">
        <li>
            <a href="${param.backUrl}">back</a>
        </li>
        <li>
            <a href="#">new playlist</a>
        </li>
        <li style="float:right;">
            <c:if test="${sortOrder != 'Album'}"><a href="#" onClick="sort('Album')"><fmt:message key="select.group.album" /></a></c:if>
            <c:if test="${sortOrder != 'Artist'}"><a href="#" onClick="sort('Artist')"><fmt:message key="select.group.artist" /></a></c:if>
        </li>
    </ul>

    <form id="browse" action="${servletUrl}/addTracks" method="post">

        <input type="hidden" name="sortOrder" value="${sortOrder}" />
        <input type="hidden" name="searchTerm" value="${param.searchTerm}" />
        <input type="hidden" name="album" value="${param.album}" />
        <input type="hidden" name="artist" value="${param.artist}" />
        <input type="hidden" name="backUrl" value="${param.backUrl}" />

        <table cellspacing="0">
            <c:forEach items="${tracks}" var="track">
                <c:if test="${track.newSection}">
                    <c:set var="count" value="0" />
                    <tr>
                        <c:if test="${false}">
                            <th class="check"><input type="checkbox" name="none" value="none" onClick="selectAll('${section.sectionIds}',this)" />
                            </th>
                        </c:if>
                        <th class="active" colspan="2">
                            <c:choose>
                                <c:when test="${sortOrder == 'Album'}">
                                    <c:if test="${track.simple}">
                                        <c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" /> - </c:if>
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" />
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
                                    <c:if test="${track.simple}">
                                        - <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" />
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </th>
                    </tr>
                </c:if>
                <tr class="${cwfn:choose(count % 2 == 1, '', 'odd')}">
                    <c:if test="${false}">
                        <td class="check">
                            <input type="checkbox" id="item${track.id}" name="track" value="${track.id}"
                                    <c:if test="${selectedTracks[track.id]}">checked="checked"</c:if> />
                        </td>
                    </c:if>
                    <td class="artist">
                        <c:choose>
                            <c:when test="${sortOrder == 'Album'}">
                                <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                                <c:if test="${!track.simple}"><c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
                                    -</c:if>
                                <c:out value="${cwfn:choose(mtfn:unknown(track.name), '(unknown)', track.name)}" />
                            </c:when>
                            <c:otherwise>
                                <c:if test="${!track.simple}"><c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" /> -
                                </c:if>
                                <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                                <c:out value="${cwfn:choose(mtfn:unknown(track.name), '(unknown)', track.name)}" />
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="icon">
                        <a href="${servletUrl}/playTrack/track=${track.id}/${cwfn:urlEncode(mtfn:virtualName(track.file), 'UTF-8')}">
                            <img src="${appUrl}/images/play${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="select.play"/>" />
                        </a>
                    </td>
                </tr>
                <c:set var="count" value="${count + 1}" />
            </c:forEach>
        </table>

        <div class="buttons">
            <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/createRSS/mytunesrss.xml'" value="RSS" />
            <input type="submit" onClick="document.forms['browse'].action = '${servletUrl}/createM3U/mytunesrss.m3u'" value="M3U" />
        </div>

    </form>

</div>

</body>

</html>