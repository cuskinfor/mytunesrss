<html>
<head>
<script src="${appUrl}/js/jquery.js" type="text/javascript"></script>
<script src="${appUrl}/js/jquery.json.js" type="text/javascript"></script>
<script src="${appUrl}/iphone/js/dynamic-html.js" type="text/javascript"></script>
<script type="text/javascript">
    var $jQ = jQuery.noConflict();
    var first;
    var pageSize = 10;
    function loadItems(page) {
        first = 0;
        top.mytunesrss('GenreService.getGenres', [page,-1,-1], function(json){top.json=json;buildList()});
    }
    function play(name) {
        top.mytunesrss("AlbumService.getAlbums", [null,null,name,-1,-1,-1,false,-1,-1], function(json) {top.json=json;top.loadContent("${appUrl}/iphone/albums.jsp?first=0")});
    }
    function buildList() {
        var json = top.json;
        var html = "<table cellspacing='3px' cellpadding='1px' width='100%'>";
        var list = json.result.results;
        for (var i = first; i < list.length && i < first + pageSize; i++) {
            var map = list[i];
            var col = (i % 2 == 0) ? "#CCCCCC" : "#EEEEEE";
            html += "<tr style='background-color:" + col + "'><td  height='42px' valign='middle' width='100%' style='padding:5px' onclick=\"play('" + map.name.replace("'", "\\'") + "')\"><b>" + getDisplayName(map.name) + "</td></tr>";
        }
        html += "</table>";
        $jQ('#genres').html(html);
        $jQ('#pager1').html(createPager("<td align='center' onclick='top.loadContent(\"${appUrl}/iphone/portal.jsp\")'><input type=submit style='font-size:14px; width:64px' value='menu'></td>", json.result.results.length, pageSize));
        $jQ('#pager2').html(createPager("<td align='center' onclick='document.location.href=\"#top\"'><input type=submit style='font-size:14px; width:64px' value='top'></td>", json.result.results.length, pageSize));
    }
</script>
</head>
<body onload="top.initScroll();first=parseInt(top.getRequestParameter(this.location.search, 'first'));$jQ('register').html(createRegister());buildList()">

<a name="top"></a>

	<table border="0" width="100%"><tr>
	<td align="center" background="${appUrl}/iphone/img/toolbar.png" style="font-weight:bold;color:white;font-size:24px" height="38px">Genres</td></tr>
	<TR><TD height="5px"></TD></TR>
	</table>

    <div id="register" style="font-size:20px"></div>
    <div id="pager1" style="font-size:32px;margin-top:10px"></div>
    <div id="genres" style="font-size:24px;margin-top:10px"></div>
    <div id="pager2" style="font-size:32px;margin-top:10px"></div>
</body>
</html>
