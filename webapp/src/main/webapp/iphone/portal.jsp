<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <script src="${appUrl}/js/jquery.js" type="text/javascript"></script>
    <script src="${appUrl}/js/jquery.json.js" type="text/javascript"></script>
    <script type="text/javascript">
        var $jQ=jQuery.noConflict();
        function loggedOut(json) {
            top.setSessionId("");
            top.document.cookie = "mtr_iphone_username=";
            top.document.cookie = "mtr_iphone_password=";
            top.init();
        }
    </script>
</head>
<body onload="top.initScroll()">
    <table border="0" cellspacing="0px" cellpadding="0px" width="100%">
	<tr>
	 <td colspan="2" align="center" background="${appUrl}/iphone/img/toolbar.png" style="font-weight:bold;color:white;font-size:24px" height="38px">MyTunesRSS Portal</td></tr>
        <tr>
            <td width="100px" height="60px" align="center"><img src="${appUrl}/iphone/img/albums.png"></td>
			<td align="center" style="font-size:26px" onclick="top.mytunesrss('AlbumService.getAlbums', [null,null,null,1,-1,-1,false,-1,-1], function(json) {top.json=json;top.loadContent('${appUrl}/iphone/albums.jsp?first=0')})">Albums</td>
        </tr>
		<TR><TD colspan="2"><img src="${appUrl}/iphone/img/line.png" width="100%" height="1px"></TD></TR>
        <tr>
			<td width="100px" height="60px" align="center"><img src="${appUrl}/iphone/img/artists.png"></td>
            <td align="center" style="font-size:26px" onclick="top.mytunesrss('ArtistService.getArtists', [null,null,null,1,-1,-1], function(json) {top.json=json;top.loadContent('${appUrl}/iphone/artists.jsp?first=0')})">Artists</td>
        </tr>
		<TR><TD colspan="2"><img src="${appUrl}/iphone/img/line.png" width="100%" height="1px"></TD></TR>
        <tr>
			<td width="100px" height="60px" align="center"><img src="${appUrl}/iphone/img/genres.png"></td>
            <td align="center" style="font-size:26px" onclick="top.mytunesrss('GenreService.getGenres', [1,-1,-1], function(json) {top.json=json;top.loadContent('${appUrl}/iphone/genres.jsp?first=0')})">Genres</td>
        </tr>
		<TR><TD colspan="2"><img src="${appUrl}/iphone/img/line.png" width="100%" height="1px"></TD></TR>
        <tr>
			<td width="100px" height="60px" align="center"><img src="${appUrl}/iphone/img/playlist.png"></td>
            <td align="center" style="font-size:26px" onclick="top.mytunesrss('PlaylistService.getPlaylists', null, function(json) {top.json=json;top.loadContent('${appUrl}/iphone/playlists.jsp?first=0')})">Playlists</td>
        </tr>
		<TR><TD colspan="2"><img src="${appUrl}/iphone/img/line.png" width="100%" height="1px"></TD></TR>
        <tr>
            <td height="60px" style="font-size:26px" onclick="top.mytunesrss('TrackService.search', [$jQ('#searchTerm').val(), 30, 'Album', 0, -1], function(json) {top.json=json;top.loadContent('${appUrl}/iphone/tracklist.jsp?first=0')})"><input type=submit style="font-size:16px" value="Search"></td> <td width="70%"><input style="height:28px; width:100%; font-size:16px" type="text" id="searchTerm" autocorrect="off" autocapitalize="off" size="30" /></td>
        </tr>
        <c:if test="${empty globalConfig.autoLogin}">
            <TR><TD colspan="2"><img src="${appUrl}/iphone/img/line.png" width="100%" height="1px"></TD></TR>
            <tr>
                <td valign="bottom" colspan="2" align="center" onclick="top.mytunesrss('LoginService.logout', null, loggedOut)"> <BR>
                <input type=submit style="font-size:16px" value="Log off"></td>
            </tr>
        </c:if>
    </table>
	&nbsp;<P>&nbsp;<BR>
</body>
</html>
