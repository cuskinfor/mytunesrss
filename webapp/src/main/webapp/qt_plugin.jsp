<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<body>

    <div align="center">
        <img src="${appUrl}/images/index_head.gif" width="100%" alt="MyTunesRSS" onclick="self.document.location.href='${servletUrl}/showPortal/${auth}'"/>
    </div>

    <div style="text-align:center;margin-top:70px">

        <embed src="${appUrl}/images/movie_poster.png" href="<c:out value="${mtfn:playbackLink(pageContext, tracks[0], null)}"/>" type="${mtfn:contentType(config, authUser, tracks[0])}" target="myself"
            <c:forEach items="${tracks}" var="track" varStatus="trackLoopStatus" begin="1">
                qtnext${trackLoopStatus.index}="<c:out value="${mtfn:playbackLink(pageContext, track, null)}"/>> T<myself>"
            </c:forEach>
            qtnext${fn:length(tracks)}="GOTO0"
        />

    </div>

</body>

</html>
