<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<%--@elvariable id="appUrl" type="java.lang.String"--%>
<%--@elvariable id="servletUrl" type="java.lang.String"--%>
<%--@elvariable id="auth" type="java.lang.String"--%>
<%--@elvariable id="encryptionKey" type="javax.crypto.SecretKey"--%>
<%--@elvariable id="config" type="de.codewave.mytunesrss.servlet.WebConfig"--%>

<html>
<head>
<title>MyTunesRSS Jukebox</title>
</head>
<body style="padding:0 0 0 0; margin:0 0 0 0">
<c:choose>
    <c:when test="${config.flashplayerType eq 'jw'}">
        <embed
          src="${appUrl}/flashplayer/mediaplayer-4-6.swf"
          width="100%"
          height="100%"
          allowscriptaccess="always"
          allowfullscreen="true"
          flashvars="file=${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${cwfn:decode64(param.playlistParams)}/playerRequest=true/type=Xspf/jwplayer=true</mt:encrypt>/${cwfn:encodeUrl(param.filename)}&amp;linktarget=_blank&amp;playlist=right&amp;autostart=true&amp;playlistsize=350&amp;repeat=list"
        ></embed>
    </c:when>
    <c:when test="${config.flashplayerType eq 'jw43'}">
        <embed
          src="${appUrl}/flashplayer/mediaplayer-4-3.swf"
          width="100%"
          height="100%"
          allowscriptaccess="always"
          allowfullscreen="true"
          flashvars="file=${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${cwfn:decode64(param.playlistParams)}/playerRequest=true/type=Xspf/jwplayer=true</mt:encrypt>/${cwfn:encodeUrl(param.filename)}&amp;linktarget=_blank&amp;playlist=right&amp;autostart=true&amp;playlistsize=350&amp;repeat=list"
        ></embed>
    </c:when>
    <c:when test="${config.flashplayerType eq 'jw3'}">
        <embed
          src="${appUrl}/flashplayer/mediaplayer-3-15-cw.swf?file=${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${cwfn:decode64(param.playlistParams)}/playerRequest=true/type=Xspf</mt:encrypt>/${cwfn:encodeUrl(param.filename)}&amp;linktarget=_blank"
          width="100%"
          height="100%"
          allowscriptaccess="always"
          allowfullscreen="true"
          flashvars="displaywidth=256"
        ></embed>
    </c:when>
    <c:otherwise>
        <embed
          src="${appUrl}/flashplayer/xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url=${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${cwfn:decode64(param.playlistParams)}/playerRequest=true/type=Xspf</mt:encrypt>/${cwfn:encodeUrl(param.filename)}"
          width="100%"
          height="100%"
          allowscriptaccess="always"
          allowfullscreen="true"
          flashvars="displaywidth=256"
        ></embed>
    </c:otherwise>
</c:choose>
</body>
</html>
