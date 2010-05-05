<html>
<head>
<script src="${appUrl}/js/jquery.js" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.json.js" type="text/javascript"></script>
<script src="${appUrl}/iphone/js/dynamic-html.js" type="text/javascript"></script>
<script type="text/javascript">
    var $jQ = jQuery.noConflict();
    var first;
    var pageSize = 10;
    function play(id) {
        top.mytunesrss("PlaylistService.getTracks", [id, null], function(json) {top.createIphonePlaylist('${appUrl}', json, 'top.loadContent(\"${appUrl}/iphone/playlists.jsp?first=' + first + '\")')});
    }
    function showTracklist(id) {
        top.mytunesrss("PlaylistService.getTracks", [id, null], function(json) {top.json=json;top.loadContent("${appUrl}/iphone/tracklist.jsp?first=0")});
    }
    function buildList() {
        var json = top.json;
        var html = "<table cellspacing='3px' cellpadding='1px' width='100%'>";
        var list = json.result.results;
        for (var i = first; i < list.length && i < first + pageSize; i++) {
            var map = list[i];
            var col = (i % 2 == 0) ? "#CCCCCC" : "#EEEEEE";
            html += "<tr style='background-color:" + col + "'><td height='42px' valign='middle' width='100%' style='padding:5px' onclick=\"showTracklist('" + map.id + "')\"><b>" + getDisplayName(map.name) + "</td></tr>";
        }
        html += "</table>";
        $jQ('#playlists').html(html);
        $jQ('#pager1').html(createPager("<td align='center' onclick='top.loadContent(\"${appUrl}/iphone/portal.jsp\")'><input type=submit style='font-size:14px; width:64px' value='menu'></td>", json.result.results.length, pageSize));
        $jQ('#pager2').html(createPager("<td align='center' onclick='document.location.href=\"#top\"'><input type=submit style='font-size:14px; width:64px' value='top'></td>", json.result.results.length, pageSize));
    }
</script>
</head>
<body onload="top.initScroll();first=parseInt(top.getRequestParameter(this.location.search, 'first'));buildList()">

	<a name="top"></a>

	<table border="0" width="100%"><tr>
	<td align="center" background="${appUrl}/iphone/img/toolbar.png" style="font-weight:bold;color:white;font-size:24px" height="38px">Playlists</td></tr>
	<TR><TD height="5px"></TD></TR>
	</table>

    <div id="pager1" style="font-size:32px;margin-top:10px"></div>
    <div id="playlists" style="font-size:24px;margin-top:10px"></div>
    <div id="pager2" style="font-size:32px;margin-top:10px"></div>
</body>
</html>
