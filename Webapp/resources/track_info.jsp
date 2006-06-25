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

    <title>
        <fmt:message key="applicationTitle" />
        v${cwfn:sysprop('mytunesrss.version')}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" href="${appUrl}/styles/mytunesrss.css" />
    <!--[if IE]>
      <link rel="stylesheet" type="text/css" href="${appUrl}/styles/ie.css" />
    <![endif]-->
    <script src="${appUrl}/js/functions.js" type="text/javascript"></script>

</head>

<body>

<div class="body">

    <h1 class="info">
        <a class="portal" href="${servletUrl}/showPortal">
            <fmt:message key="portal" />
        </a> <span><fmt:message key="myTunesRss" /></span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

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
                <c:out value="${mtfn:virtualSuffix(config, track)}" />
            </td>
        </tr>
        <tr class="odd">
            <td>
                &nbsp;
            </td>
            <td>
                <a href="${servletUrl}/playTrack/authHash=${authHash}/track=${track.id}/${mtfn:virtualTrackName(track)}.${mtfn:virtualSuffix(config, track)}">
                    <img src="${appUrl}/images/download_odd.gif" alt="<fmt:message key="download"/>" />
                    <fmt:message key="doDownload"/>
                </a>
            </td>
        </tr>
    </table>

</div>

</body>

</html>