<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">

<head>

    <jsp:include page="incl_head.jsp"/>

</head>

<body>

    <div class="body">

        <h1 class="search"><span><fmt:message key="myTunesRss" /></span></h1>

        <object classid="clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B" width="100%" height="16"
                codebase="http://www.apple.com/qtactivex/qtplugin.cab">
            <param name="controller" value="true" />
            <param name="autoplay" value="true" />
            <param name="kioskmode" value="true" />
            <param name="type" value="video/quicktime" />
            <embed src="${servletUrl}/playTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(tracks[0].id)}/tc=${mtfn:tcParamValue(config, authUser, tracks[0])}/playerRequest=${param.playerRequest}</mt:encrypt>/${mtfn:virtualTrackName(tracks[0])}.${mtfn:suffix(config, authUser, tracks[0])}"
                   controller="true" autoplay="true" kioskmode="true" width="100%" height="16" type="video/quicktime"
                <c:forEach items="${tracks}" var="item" begin="1" varStatus="itemLoopStatus">
                    qtnext${itemLoopStatus.index}="<${servletUrl}/playTrack/${auth}/<mt:encrypt key="${encryptionKey}">track=${cwfn:encodeUrl(item.id)}/tc=${mtfn:tcParamValue(config, authUser, item)}/playerRequest=${param.playerRequest}</mt:encrypt>/${mtfn:virtualTrackName(item)}.${mtfn:suffix(config, authUser, item)}>T<myself>"
                </c:forEach>
                >
            </embed>
        </object>

    </div>

</body>

</html>
