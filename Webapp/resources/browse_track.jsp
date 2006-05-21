<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<c:set var="backUrl" scope="request">${servletUrl}/browseTrack?album=${param.album}&artist=${param.artist}&searchTerm=${param.searchTerm}&index=${param.index}&backUrl=${cwfn:urlEncode(param.backUrl, 'UTF-8')}&sortOrder=${sortOrder}</c:set>

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

<h1 class="<c:choose><c:when test="${!empty param.searchTerm}">searchResult</c:when><c:otherwise>browse</c:otherwise></c:choose>">
    <a class="portal" href="${servletUrl}/showPortal">Portal</a> <span>MyTunesRSS</span>
</h1>

<jsp:include page="/incl_error.jsp" />

<ul class="links">
    <c:if test="${sortOrderLink}">
        <li>
            <c:if test="${sortOrder != 'Album'}"><a href="#" onClick="sort('Album')"><fmt:message key="select.group.album" /></a></c:if>
            <c:if test="${sortOrder != 'Artist'}"><a href="#" onClick="sort('Artist')"><fmt:message key="select.group.artist" /></a></c:if>
        </li>
    </c:if>
    <c:if test="${empty sessionScope.playlist}">
        <li>
            <a href="${servletUrl}/startNewPlaylist?backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">new playlist</a>
        </li>
    </c:if>
    <li style="float:right;">
        <a href="${param.backUrl}">back</a>
    </li>
</ul>

<jsp:include page="incl_playlist.jsp" />

<form id="browse" action="${servletUrl}/addToPlaylist" method="post">

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
        <c:if test="${!empty sessionScope.playlist}">
            <th class="check"><input type="checkbox" name="none" value="none" onClick="selectAll('${track.sectionIds}',this)" />
            </th>
        </c:if>
        <th class="active" colspan="2">
            <c:choose>
                <c:when test="${sortOrder == 'Album'}">
                    <c:if test="${track.simple}">
                        <c:set var="sectionFileName">${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)} -</c:set>
                        <a href="${servletUrl}/browseAlbum?artist=<c:out value="${cwfn:urlEncode(track.artist, 'UTF-8')}"/>">
                            <c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
                        </a> -</c:if>
                    <c:set var="sectionFileName">${sectionFileName} ${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}</c:set>
                    <c:choose>
                        <c:when test="${empty param.album}">
                            <a href="${servletUrl}/browseTrack?album=<c:out value="${cwfn:urlEncode(track.album, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                                <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" />
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <a href="${servletUrl}/browseAlbum?artist=<c:out value="${cwfn:urlEncode(track.artist, 'UTF-8')}"/>">
                        <c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
                    </a>
                    <c:set var="sectionFileName" value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
                    <c:if test="${track.simple}">
                        <c:set var="sectionFileName">${sectionFileName} - ${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}</c:set>
                        -
                        <c:choose>
                            <c:when test="${empty param.album}">
                                <a href="${servletUrl}/browseTrack?album=<c:out value="${cwfn:urlEncode(track.album, 'UTF-8')}"/>&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                                    <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" />
                                </a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" />
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </th>
        <c:choose>
            <c:when test="${empty sessionScope.playlist}">
                <c:forEach items="${config.feedTypes}" var="feedType">
                    <th class="icon">
                        <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/tracklist=${cwfn:urlEncode(track.sectionIds, 'UTF-8')}/${mtfn:cleanFileName(sectionFileName)}.${config.feedFileSuffix[feedType]}">
                            <img src="${appUrl}/images/${feedType}_th.gif" alt="${feedType}" /> </a>
                    </th>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <th class="icon">
                    <a href="${servletUrl}/addToPlaylist?tracklist=${cwfn:urlEncode(track.sectionIds, 'UTF-8')}&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                        <img src="${appUrl}/images/add_th.gif" alt="add" /> </a>
                </th>
            </c:otherwise>
        </c:choose>
    </tr>
</c:if>
<tr class="${cwfn:choose(count % 2 == 1, 'even', 'odd')}">
    <c:if test="${!empty sessionScope.playlist}">
        <td class="check">
            <input type="checkbox" id="item${track.id}" name="track" value="${track.id}"<c:if test="${selectedTracks[track.id]}">
                checked="checked"</c:if> />
        </td>
    </c:if>
    <td class="artist" <c:if test="${!(sortOrder == 'Album' && !track.simple)}">colspan="2"</c:if>>
        <c:choose>
            <c:when test="${sortOrder == 'Album'}">
                <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                <c:out value="${cwfn:choose(mtfn:unknown(track.name), '(unknown)', track.name)}" />
            </c:when>
            <c:otherwise>
                <c:if test="${!track.simple}"><c:out value="${cwfn:choose(mtfn:unknown(track.album), '(unknown)', track.album)}" /> - </c:if>
                <c:if test="${track.trackNumber > 0}">${track.trackNumber} -</c:if>
                <c:out value="${cwfn:choose(mtfn:unknown(track.name), '(unknown)', track.name)}" />
            </c:otherwise>
        </c:choose>
    </td>
		<c:if test="${sortOrder == 'Album' && !track.simple}">
			<td>
				<a href="${servletUrl}/browseAlbum?artist=${cwfn:urlEncode(track.artist, 'UTF-8')}">
					<c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
				</a>
			</td>
		</c:if>
    <c:choose>
        <c:when test="${empty sessionScope.playlist}">
            <c:forEach items="${config.feedTypes}" var="feedType">
                <td class="icon">
                    <a href="${servletUrl}/create${fn:toUpperCase(feedType)}/track=<c:out value="${cwfn:urlEncode(track.id, 'UTF-8')}"/>/${mtfn:virtualTrackName(track)}.${config.feedFileSuffix[feedType]}">
                        <img src="${appUrl}/images/${feedType}${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="${feedType}" /> </a>
                </td>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <td class="icon">
                <a href="${servletUrl}/addToPlaylist?track=${track.id}&backUrl=${cwfn:urlEncode(backUrl, 'UTF-8')}">
                    <img src="${appUrl}/images/add${cwfn:choose(count % 2 == 0, '', '_odd')}.gif" alt="add" /> </a>
            </td>
        </c:otherwise>
    </c:choose>
</tr>
<c:set var="count" value="${count + 1}" />
</c:forEach>
</table>

<c:if test="${!empty pager}">
    <c:set var="pagerCommand" scope="request" value="${servletUrl}/browseTrack?album=${param.album}&artist=${param.artist}&searchTerm=${param.searchTerm}&index={index}&backUrl=${cwfn:urlEncode(param.backUrl, 'UTF-8')}&sortOrder=${sortOrder}" />
    <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
    <jsp:include page="incl_bottomPager.jsp" />
</c:if>

<c:if test="${!empty sessionScope.playlist}">
    <div class="buttons">
        <input type="submit"
               onClick="document.forms['browse'].action = '${servletUrl}/addToPlaylist';document.forms['browse'].elements['backUrl'].value = '${backUrl}'"
               value="add selected" />
    </div>
</c:if>

</form>

</div>

</body>

</html>