<html>

<head>

    <script type="text/javascript" src="http://www.google.com/jsapi"></script>
    <script type="text/javascript" src="${appUrl}/js/jquery.js"></script>
    <script type="text/javascript" src="${appUrl}/jqtouch/jqtouch/jqtouch.min.js"></script>
    <script src="${appUrl}/js/jquery.json.js" type="text/javascript"></script>
    <script src="${appUrl}/js/functions.js" type="text/javascript"></script>
    <style type="text/css" media="screen">@import "${appUrl}/jqtouch/jqtouch/jqtouch.min.css";</style>
    <style type="text/css" media="screen">@import "${appUrl}/jqtouch/themes/apple/theme.min.css";</style>
    <script type="text/javascript">
        var sessionId = '';
        var paramsAlbumServiceGetAlbums;
        var $jQ = jQuery.noConflict();
        $jQ.jQTouch({
            icon: '${appUrl}/iphone/img/icon.png',
            startupScreen: '${appUrl}/iphone/img/splash.png',
            statusBar: 'black-translucent',
            preloadImages: [
                '${appUrl}/jqtouch/themes/jqt/img/chevron_white.png',
                '${appUrl}/jqtouch/themes/jqt/img/bg_row_select.gif',
                '${appUrl}/jqtouch/themes/jqt/img/back_button_clicked.png',
                '${appUrl}/jqtouch/themes/jqt/img/button_clicked.png'
            ]
        });
        $jQ(document).ready(function(e) {
            ajaxCall("LoginService.login", ['mdescher', 'Im2faf4U', 180], function (result, error) {
                if (!error) {
                    sessionId = result;
                    $jQ('#playlists').bind('pageAnimationStart', function(event, info) {
                        if (info.direction == 'in') {
                            loadPlaylists()
                        } else if (info.direction == 'out') {
                            unloadPlaylists();
                        }
                        $jQ(this).data('referrer'); // return the link which triggered the animation, if possible
                    });
                    $jQ('#albums').bind('pageAnimationStart', function(event, info) {
                        if (info.direction == 'in') {
                            loadAlbums()
                        } else if (info.direction == 'out') {
                            unloadAlbums();
                        }
                        $jQ(this).data('referrer'); // return the link which triggered the animation, if possible
                    });
                    $jQ('#artists').bind('pageAnimationStart', function(event, info) {
                        if (info.direction == 'in') {
                            loadArtists()
                        } else if (info.direction == 'out') {
                            unloadArtists();
                        }
                        $jQ(this).data('referrer'); // return the link which triggered the animation, if possible
                    });
                    $jQ('#genres').bind('pageAnimationStart', function(event, info) {
                        if (info.direction == 'in') {
                            loadGenres()
                        } else if (info.direction == 'out') {
                            unloadGenres();
                        }
                        $jQ(this).data('referrer'); // return the link which triggered the animation, if possible
                    });
                } else {
                    alert('Login failed: ' + error);
                }
            });
        });
        function ajaxCall(func, parameterArray, callback) {
            jsonRpc('${appUrl}/jsonrpc', func, parameterArray, callback, sessionId)
        }
        function getDisplayName(name) {
            if (name == 'undefined' || name == null || name == '!') {
                return 'Unknown';
            }
            return name;
        }
        function loadPlaylists() {
            ajaxCall('PlaylistService.getPlaylists', [], function(result, error) {
                var html = '';
                var list = result.results;
                for (var i = 0; i < list.length; i++) {
                    var map = list[i];
                    html += '<li><a href="#">' + getDisplayName(map.name) + '</a></li>';
                }
                html += '</ul>';
                $jQ('#playlists > ul').html(html);
            });
        }
        function unloadPlaylists() {
            $jQ('#playlists > ul').html('');
        }
        function loadAlbums() {
            ajaxCall('AlbumService.getAlbums', [null, null, null, 1, -1, -1, false, -1, -1], function(result, error) {
                var html = '<ul class="edgetoedge">';
                var list = result.results;
                for (var i = 0; i < list.length; i++) {
                    var map = list[i];
                    html += '<li><a href="#">' + getDisplayName(map.name) + '</a></li>';
                }
                $jQ('#albums > ul').html(html);
            });
        }
        function unloadAlbums() {
            $jQ('#albums > ul').html('');
        }
        function loadArtists() {
            ajaxCall('ArtistService.getArtists', [null, null, null, 1, -1, -1], function(result, error) {
                var html = '';
                var list = result.results;
                for (var i = 0; i < list.length; i++) {
                    var map = list[i];
                    html += '<li><a href="#albums">' + getDisplayName(map.name) + '</a></li>';
                }
                $jQ('#artists > ul').html(html);
            });
        }
        function unloadArtists() {
            $jQ('#artists > ul').html('');
        }
        function loadGenres() {
            ajaxCall('GenreService.getGenres', [1, -1, -1], function(result, error) {
                var html = '';
                var list = result.results;
                for (var i = 0; i < list.length; i++) {
                    var map = list[i];
                    html += '<li><a href="#">' + getDisplayName(map.name) + '</a></li>';
                }
                $jQ('#genres > ul').html(html);
            });
        }
        function unloadGenres() {
            $jQ('#genres > ul').html('');
        }
    </script>
</head>

<body>

<div id="main">
    <div class="toolbar">
        <h1>MyTunesRSS</h1>
    </div>
    <ul class="rounded">
        <li><a href="#playlists">Playlists</a></li>
        <li><a href="#albums">Albums</a></li>
        <li><a href="#artists">Artists</a></li>
        <li><a href="#genres">Genres</a></li>
    </ul>
    <ul class="rounded">
        <li><a href="#search">Search</a></li>
    </ul>
    <ul class="rounded">
        <li><a href="#logout">Logout</a></li>
    </ul>
</div>

<div id="playlists">
    <div class="toolbar">
        <h1>Playlists</h1>
        <a class="back" href="#main">Portal</a>
    </div>
    <ul class="edgetoedge"></ul>
</div>

<div id="albums">
    <div class="toolbar">
        <h1>Albums</h1>
        <a class="back" href="#main">Portal</a>
    </div>
    <ul class="edgetoedge"></ul>
</div>

<div id="artists">
    <div class="toolbar">
        <h1>Artists</h1>
        <a class="back" href="#main">Portal</a>
    </div>
    <ul class="edgetoedge"></ul>
</div>

<div id="genres">
    <div class="toolbar">
        <h1>Genres</h1>
        <a class="back" href="#main">Portal</a>
    </div>
    <ul class="edgetoedge"></ul>
</div>

</body>

</html>
