<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

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
    <script src="${appUrl}/js/functions.js?ts=${sessionCreationTime}" type="text/javascript"></script>

</head>

<body>

<div class="body">

    <h1 class="info">
        <a class="portal" href="${servletUrl}/showPortal">
            <fmt:message key="portal" />
        </a> <span><fmt:message key="myTunesRss" /></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

  <ul class="links">
    <c:if test="${!empty param.backUrl}">
      <li>
        <a href="${param.backUrl}"><fmt:message key="back" /></a>
      </li>
    </c:if>
  </ul>

    <table cellspacing="0">
        <tr>
            <th colspan="2" class="active">
                <c:out value="${track.artist}" />
                -
                <c:out value="${track.name}" />
            </th>
        </tr>
        <tr>
            <td>
                <fmt:message key="artist" />:
            </td>
            <td>
                <c:out value="${track.artist}" />
            </td>
        </tr>
        <tr class="odd">
            <td>
                <fmt:message key="track" />:
            </td>
            <td>
                <c:out value="${track.name}" />
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="album" />:
            </td>
            <td>
                <c:out value="${track.album}" />
            </td>
        </tr>
        <tr class="odd">
            <td>
                <fmt:message key="duration" />:
            </td>
            <td>
                <c:out value="${mtfn:duration(track)}" />
            </td>
        </tr>
        <tr>
            <td><fmt:message key="type"/>:</td>
            <td>
                <c:if test="${track.protected}"><img src="${appUrl}/images/protected.gif" alt="<fmt:message key="protected"/>" style="vertical-align:middle" /></c:if>
                <c:if test="${track.video}"><img src="${appUrl}/images/movie.gif" alt="<fmt:message key="video"/>" style="vertical-align:middle" /></c:if>
                <c:out value="${mtfn:suffix(track)}" />
            </td>
        </tr>
        <c:if test="${authUser.download && config.showDownload}">
            <tr class="odd">
                <td>
                    &nbsp;
                </td>
                <td>
                    <a href="${servletUrl}/playTrack/auth=${cwfn:encodeUrl(auth)}/track=${cwfn:encodeUrl(track.id)}/${mtfn:virtualTrackName(track)}.${mtfn:suffix(track)}">
                        <img src="${appUrl}/images/download_odd.gif" alt="<fmt:message key="download"/>" />
                        <fmt:message key="doDownload"/>
                    </a>
                </td>
            </tr>
        </c:if>
        <tr <c:if test="${!authUser.download || !config.showDownload}">class="odd"</c:if>>
          <td colspan="2">
            <img alt="${track.name} Album Art"
              src="${servletUrl}/showTrackImage/auth=${cwfn:encodeUrl(auth)}/track=${cwfn:encodeUrl(track.id)}"
              width="200" style="display: block; margin: 10px auto;"/>
          </td>
        </tr>
    </table>

</div>

</body>

</html>