<html>
<head>
<script src="${appUrl}/iphone/js/prototype.js" type="text/javascript"></script>
<script src="${appUrl}/iphone/js/dynamic-html.js" type="text/javascript"></script>
<script type="text/javascript">
var first;
var pageSize = 10;
var currentList;
function playPlaylist(id) {
    top.mytunesrss("PlaylistService.getTracks", [id, null], function(json) {top.createIphonePlaylist(getTracks(), 0, 'top.loadContent(\"${appUrl}/iphone/tracklist.jsp?first=' + first + '\")')});
}
function playTracks(startIndex) {
    top.createIphonePlaylist(getTracks(), startIndex, 'top.loadContent(\"${appUrl}/iphone/tracklist.jsp?first=' + first + '\")');
}
function getTracks() {
    var currentList = top.json.result.tracks;
    if (currentList == undefined) {
        currentList = top.json.result.results;
        if (currentList == undefined) {
            top.clientError("JSON structure does not contain tracks!");
            return;
        }
    }
    return currentList;
}
function buildList() {
    var json = top.json;
    var html = "<table cellspacing='3px' cellpadding='1px' width='100%'>";
    currentList = getTracks();
    for (var i = first; i < currentList.length && i < first + pageSize; i++) {
        var map = currentList[i];
        var col = (i % 2 == 0) ? "#CCCCCC" : "#EEEEEE";
        if (map.newSection == true) {
            html += "<tr style='background-color:#CCCCFF'>";
            html += map.imageUrl ? "<td widht='48px' height='48px'><img src='" + map.imageUrl + "/size=64' width='48px' height='48px' /></td><td " : "<td colspan='2' ";
            html += "valign='middle' width='100%' style='padding:5px' onclick=\"playTracks(" + i + ")\"><b>" + getDisplayName(map.album) + "<br />" + getDisplayName(map.artist) + "</b></td></tr>";
        }
        html += "<tr style='background-color:" + col + "'>";
        html += map.imageUrl ? "<td widht='48px' height='48px'><img src='" + map.imageUrl + "/size=64' width='48px' height='48px' /></td><td " : "<td colspan='2' ";
        html += "valign='middle' width='100%' style='padding:5px' onclick=\"playTracks(" + i + ")\"><b>" + getDisplayName(map.name) + "</b><br />" + getDisplayName(map.artist) + "</td></tr>";
    }
    html += "</table>";
    $('tracklist').innerHTML = html;
    $('pager1').innerHTML = createPager("<td align='center' onclick='top.loadContent(\"${appUrl}/iphone/portal.jsp\")'><input type=submit style='font-size:14px; width:64px' value='menu'></td>", currentList.length, pageSize);
    $('pager2').innerHTML = createPager("<td align='center' onclick='document.location.href=\"#top\"'><input type=submit style='font-size:14px; width:64px' value='top'></td>", currentList.length, pageSize);
}
</script>
</head>
<body onload="top.initScroll();first=parseInt(top.getRequestParameter(this.location.search, 'first'));buildList()">
    <a name="top"></a>
    <div id="pager1" style="font-size:32px;margin-top:10px"></div>
    <div id="tracklist" style="font-size:24px;margin-top:10px"></div>
    <div id="pager2" style="font-size:32px;margin-top:10px"></div>
</body>
</html>
