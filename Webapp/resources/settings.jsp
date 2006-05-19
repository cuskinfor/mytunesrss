<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>

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

</head>

<body>

<div class="body">

    <h1 class="settings">
        <a class="portal" href="${servletUrl}/showPortal">Portal</a> <span>MyTunesRSS</span>
    </h1>

    <jsp:include page="/incl_error.jsp" />

    <form action="${servletUrl}/saveSettings" method="post">
        <table cellspacing="0">
            <tr>
                <th class="active">Settings</th>
                <th>&nbsp;</th>
            </tr>
            <tr>
                <td>Fake MP3 suffix</td>
                <td><input type="text" name="suffix.mp3" value="<c:out value="${config.map['suffix.mp3']}"/>" /></td>
            </tr>
            <tr class="odd">
                <td>Fake M4A suffix</td>
                <td><input type="text" name="suffix.m4a" value="<c:out value="${config.map['suffix.m4a']}"/>" /></td>
            </tr>
            <tr>
                <td>Limit RSS feed items (0 = no limit)</td>
                <td>
                    <input type="text" name="rssFeedLimit" value="<c:out value="${config.map['rssLimit']}"/>" style="width: 50px;" />
                </td>
            </tr>
            <tr class="odd">
                <td>Playlist types</td>
                <td>
                    <input type="checkbox" name="feedType" value="rss" <c:if test="${fn:contains(config.map['feedTypes'], 'rss')}">
                        checked="checked"</c:if> /> RSS
                    <input type="checkbox" name="feedType" value="m3u" <c:if test="${fn:contains(config.map['feedTypes'], 'm3u')}">
                        checked="checked"</c:if> style="margin-left: 15px;" /> M3U </td>
            </tr>
            <tr>
                <td>Remember password</td>
                <td><input type="checkbox" name="loginCookie" value="true" <c:if test="${config.map['loginCookie']}">checked="checked"</c:if> /></td>
            </tr>
        </table>

        <div class="buttons">
            <input type="submit" value="save" />
        </div>
    </form>
</div>

</body>

</html>
