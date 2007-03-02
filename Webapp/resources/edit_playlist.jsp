<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRssWeb" />

<c:set var="backUrl">${servletUrl}/editPlaylist?allowEditEmpty=${param.allowEditEmpty}&amp;index=${param.index}&amp;backUrl=${cwfn:encodeUrl(param.backUrl)}</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <title><fmt:message key="applicationTitle" /> v${mytunesrssVersion}</title>
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

    <c:if test="${states.addToPlaylistMode}">
        <ul class="links">
            <li style="float:right;">
                <a href="${param.backUrl}"><fmt:message key="back"/></a>
            </li>
        </ul>
    </c:if>

    <jsp:include page="/incl_error.jsp" />

    <form id="playlist" action="${servletUrl}/savePlaylist" method="post">
        <table class="portal" cellspacing="0">
            <tr>
                <td class="playlistManager">
                    <fmt:message key="playlistName"/> <input type="text" name="name" value="<c:out value="${playlist.name}"/>" />
                </td>
                <td class="links">
                    <a class="add" href="${servletUrl}/continuePlaylist" style="background-image:url('${appUrl}/images/add_more.gif');"><fmt:message key="addMoreSongs"/></a>
                </td>
        </table>

        <input type="hidden" name="backUrl" value="${param.backUrl}" />
        <input type="hidden" name="allowEditEmpty" value="${param.allowEditEmpty}" />
        <table cellspacing="0">
            <tr>
                <th class="active" colspan="4"><fmt:message key="playlistContent"/></th>
            </tr>
            <c:forEach items="${tracks}" var="track" varStatus="trackLoop">
                <tr class="${cwfn:choose(trackLoop.index % 2 == 0, 'even', 'odd')}">
                    <td class="check">
                        <input type="checkbox" id="item${track.id}" name="track" value="${track.id}" />
                    </td>
                    <td>
                        <c:if test="${track.protected}"><img src="${appUrl}/images/protected${cwfn:choose(trackLoop.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle"/></c:if>
                        <c:if test="${track.video}"><img src="${appUrl}/images/movie${cwfn:choose(trackLoop.index % 2 == 0, '', '_odd')}.gif" alt="<fmt:message key="video"/>" style="vertical-align:middle"/></c:if>
                        <c:out value="${cwfn:choose(mtfn:unknown(track.name), '(unknown)', track.name)}" />
                    </td>
                    <td>
                        <c:out value="${cwfn:choose(mtfn:unknown(track.artist), '(unknown)', track.artist)}" />
                    </td>
                    <td class="icon">
                        <a href="${servletUrl}/removeFromPlaylist?allowEditEmpty=${param.allowEditEmpty}&amp;track=${cwfn:encodeUrl(track.id)}&amp;backUrl=${cwfn:encodeUrl(param.backUrl)}">
                            <img src="${appUrl}/images/delete${cwfn:choose(trackLoop.index % 2 == 0, '', '_odd')}.gif" alt="delete" /> </a>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <c:if test="${!empty pager}">
            <c:set var="pagerCommand"
                   scope="request"
                   value="${servletUrl}/editPlaylist?allowEditEmpty=${param.allowEditEmpty}&amp;index={index}&amp;backUrl=${cwfn:encodeUrl(param.backUrl)}" />
            <c:set var="pagerCurrent" scope="request" value="${cwfn:choose(!empty param.index, param.index, '0')}" />
            <jsp:include page="incl_bottomPager.jsp" />
        </c:if>

        <div class="buttons">
            <input type="button" onClick="document.forms['playlist'].action = '${servletUrl}/removeFromPlaylist';document.forms['playlist'].submit()" value="<fmt:message key="removeSelected"/>" />
            <input type="submit"
                   onClick="document.forms['playlist'].action = '${servletUrl}/savePlaylist';document.forms['playlist'].elements['backUrl'].value = '${backUrl}'"
                   value="<fmt:message key="savePlaylist"/>" />
            <input type="button" onClick="document.location.href = '${servletUrl}/cancelEditPlaylist?backUrl=${cwfn:encodeUrl(param.backUrl)}'" value="<fmt:message key="doCancel"/>" />
        </div>
    </form>

</div>

</body>

</html>