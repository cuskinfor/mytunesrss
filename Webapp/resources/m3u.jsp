<%@ page contentType="text/plain;charset=UTF-8" language="java" %><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %><fmt:setBundle basename="de.codewave.mytunesrss.MyTunesRSSWeb"/>#EXTM3U
<c:forEach items="${musicFiles}" var="item">#EXTINF:${item.seconds},${item.artist} : ${item.name}
${urlMap.mp3}/id=${item.id}${authInfo}/${cwfn:urlEncode(item.virtualFileName, 'UTF-8')}
</c:forEach>
