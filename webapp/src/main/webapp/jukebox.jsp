<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>

<html>
<head>
<title>MyTunesRSS Jukebox</title>
</head>
<body style="padding:0 0 0 0; margin:0 0 0 0">
<c:choose>
    <c:when test="${config.flashplayerType eq 'jw'}">
        <embed
          src="${appUrl}/flashplayer/mediaplayer.swf?file=${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${param.playlistParams}/playerRequest=true/type=Xspf</mt:encrypt>/${param.filename}&amp;linktarget=_blank"
          width="100%"
          height="100%"
          allowscriptaccess="always"
          allowfullscreen="true"
          flashvars="displaywidth=256"
        />
    </c:when>
    <c:otherwise>
        <embed
          src="${appUrl}/flashplayer/xspf_player.swf?autoplay=true&amp;autoload=true&amp;playlist_url=${servletUrl}/createPlaylist/${auth}/<mt:encrypt key="${encryptionKey}">${param.playlistParams}/playerRequest=true/type=Xspf</mt:encrypt>/${param.filename}"
          width="100%"
          height="100%"
          allowscriptaccess="always"
          allowfullscreen="true"
          flashvars="displaywidth=256"
        />
    </c:otherwise>
</c:choose>
</body>
</html>
