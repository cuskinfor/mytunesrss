<% String animation = "dissolve"; %>

<html>

    <head>

        <style type="text/css" media="screen">@import "${appUrl}/jqtouch/jqtouch/jqtouch.css";</style>

        <style type="text/css" media="screen">@import "${appUrl}/jqtouch/themes/apple/theme.css";</style>

        <script type="text/javascript" src="${appUrl}/js/sha1.js"></script>

        <script type="text/javascript" src="${appUrl}/js/jquery.js"></script>

        <script type="text/javascript" src="${appUrl}/jqtouch/jqtouch/jqtouch.js"></script>

        <script src="${appUrl}/js/jquery.json.js" type="text/javascript"></script>

        <script src="${appUrl}/js/functions.js" type="text/javascript"></script>

        <script type="text/javascript">

            var userAgent = navigator.userAgent.toLowerCase();
            var isIphone = (userAgent.indexOf('iphone') != -1 || userAgent.indexOf('ipod') != -1);
            var clickEvent = isIphone ? 'tap' : 'click';
            var sessionId = '';
            var $jQ = jQuery.noConflict();
            var jQT = new $jQ.jQTouch({
                icon: '${appUrl}/images/iphone_icon.png',
                startupScreen: '${appUrl}/images/iphone_splash.png',
                statusBar: 'black-translucent'
            });
            $jQ(document).ready(function(e) {
                $jQ('#jukebox').bind('pageAnimationStart', function(event, info) {
                    if (info.direction == 'out') {
                        $jQ('#qtPlugin').html('');
                    }
                    $jQ(this).data('referrer'); // return the link which triggered the animation, if possible
                });
                $jQ('#main').bind('pageAnimationStart', function(event, info) {
                    if (info.direction == 'out') {
                        $jQ('#infoMain').remove();
                    }
                    $jQ(this).data('referrer'); // return the link which triggered the animation, if possible
                });
                $jQ('#loginButton').bind(clickEvent, function(event, info) {
                    login();
                });
                $jQ('#menuPlaylists').bind(clickEvent, function(event, info) {
                    loadPlaylists('PlaylistService.getPlaylists', []);
                });
                $jQ('#menuGenres').bind(clickEvent, function(event, info) {
                    loadGenres('GenreService.getGenres', [1, -1, -1]);
                });
                $jQ('#menuSearch').bind(clickEvent, function(event, info) {
                    if ($jQ('#searchTerm').val()) {
                        jQT.goTo('#tracklist', '<%= animation %>');
                        loadTracks('TrackService.search', [$jQ('#searchTerm').val(), 30, 'KeepOrder', 0, -1]);
                    } else {
                        $jQ('#main').append('<div id="infoMain" class="info">No search terms specified!</div>');
                    }
                });
                $jQ('#menuLogout').bind(clickEvent, function(event, info) {
                    sessionId = '';
                    ajaxCall('LoginService.logout', []);
                });
                $jQ('#albumsindex > ul > li > a').each(function(index) {
                    $jQ(this).bind(clickEvent, function(event, info) {
                        loadAlbums('AlbumService.getAlbums', [null, null, null, index, -1, -1, false, -1, -1]);
                    });
                });
                $jQ('#artistsindex > ul > li > a').each(function(index) {
                    $jQ(this).bind(clickEvent, function(event, info) {
                        loadArtists('ArtistService.getArtists', [null, null, null, index, -1, -1]);
                    });
                });
            });

            function login() {
                ajaxCall("LoginService.login", [$jQ('#username').val(), $jQ('#password').val(), 180], function (result, error) {
                    if (!error) {
                        if ($jQ('#infoLogin')) {
                            $jQ('#infoLogin').remove();
                        }
                        sessionId = result;
                        jQT.goTo('#main', '<%= animation %>');
                    } else {
                        $jQ('#login').append('<div id="infoLogin" class="info">Login failed!</div>');
                    }
                });
            }

            function ajaxCall(func, parameterArray, callback) {
                jsonRpc('${appUrl}/jsonrpc', func, parameterArray, callback, sessionId)
            }

            function getDisplayName(name) {
                if (name == 'undefined' || name == null || name == '!') {
                    return 'Unknown';
                }
                return name;
            }

            function loadPlaylists(method, params) {
                $jQ('#playlists > ul').html('');
                ajaxCall(method, params, function(result, error) {
                    var html = '';
                    var list = result.results;
                    for (var i = 0; i < list.length; i++) {
                        html += '<li class="arrow"><a class="<%= animation %>" href="#tracklist">' + getDisplayName(list[i].name) + '</a></li>';
                    }
                    html += '</ul>';
                    $jQ('#playlists > ul').html(html);
                    $jQ('#playlists > ul > li > a').each(function(index) {
                        $jQ(this).bind(clickEvent, function() {
                            loadTracks('PlaylistService.getTracks', [list[index].id, null]);
                        })
                    });
                });
            }

            function loadAlbums(method, params) {
                $jQ('#albums > ul').html('');
                ajaxCall(method, params, function(result, error) {
                    var html = '';
                    var list = result.results;
                    for (var i = 0; i < list.length; i++) {
                        html += '<li class="arrow">';
                        if (list[i].imageUrl) {
                            html += '<img src="' + list[i].imageUrl + '/size=64" width="40px" height="40px" style="float: left"/>';
                        }
                        html += '<a class="<%= animation %>" style="padding-left: 60px" href="#tracklist">' + getDisplayName(list[i].name) + '<h5>';
                        if (list[i].artistCount == 1) {
                            html += getDisplayName(list[i].artist);
                        } else {
                            html += 'Various';
                        }
                        html += '</h5></a></li>';
                    }
                    $jQ('#albums > ul').html(html);
                    $jQ('#albums > ul > li > a').each(function(index) {
                        $jQ(this).bind(clickEvent, function() {
                            loadTracks('AlbumService.getTracks', [[list[index].name.replace('\'', '\\\'')]]);
                        })
                    });
                });
            }

            function loadArtists(method, params) {
                $jQ('#artists > ul').html('');
                ajaxCall(method, params, function(result, error) {
                    var html = '';
                    var list = result.results;
                    for (var i = 0; i < list.length; i++) {
                        html += '<li class="arrow"><a class="<%= animation %>" href="#albums">' + getDisplayName(list[i].name) + '</a></li>';
                    }
                    $jQ('#artists > ul').html(html);
                    $jQ('#artists > ul > li > a').each(function(index) {
                        $jQ(this).bind(clickEvent, function() {
                            loadAlbums('AlbumService.getAlbums', [null, list[index].name.replace('\'', '\\\''), null, -1, -1, -1, false, -1, -1]);
                        })
                    });
                });
            }

            function loadGenres(method, params) {
                $jQ('#genres > ul').html('');
                ajaxCall(method, params, function(result, error) {
                    var html = '';
                    var list = result.results;
                    for (var i = 0; i < list.length; i++) {
                        html += '<li class="arrow"><a class="<%= animation %>" href="#albums">' + getDisplayName(list[i].name) + '</a></li>';
                    }
                    $jQ('#genres > ul').html(html);
                    $jQ('#artists > ul > li > a').each(function(index) {
                        $jQ(this).bind(clickEvent, function() {
                            loadAlbums('AlbumService.getAlbums', [null, null, list[index].name.replace('\'', '\\\''), -1, -1, -1, false, -1, -1]);
                        })
                    });
                });
            }
            
            function loadTracks(method, params) {
                $jQ('#tracklist > ul').html('');
                ajaxCall(method, params, function(result, error) {
                    var html = '';
                    var list = result.results ? result.results : result.tracks;
                    for (var i = 0; i < list.length; i++) {
                        html += '<li><a class="<%= animation %>" href="#jukebox">' + getDisplayName(list[i].name);
                        html += '<h5>' + getDisplayName(list[i].artist) + '</h5>';
                        html += '</a></li>';
                    }
                    $jQ('#tracklist > ul').html(html);
                    $jQ('#tracklist > ul > li > a').each(function(index) {
                        $jQ(this).bind(clickEvent, function() {
                            var qtHtml = '<embed src="${appUrl}/images/movie_poster.png" autoplay="true" href="' + list[index].playbackUrl + '" type="' + list[index].contentType + '" target="myself"\n';
                            for (var i = index + 1; i < list.length; i++) {
                                qtHtml += 'qtnext' + i + '="<' + list[i].playbackUrl + '> T<myself>"\n';
                            }
                            qtHtml += 'qtnext' + list.length + '="GOTO0" />';
                            $jQ('#qtPlugin').html(qtHtml);
                        })
                    });
                });
            }

        </script>

    </head>

    <body>

        <div id="login">
            <div class="toolbar">
                <h1>MyTunesRSS</h1>
            </div>
            <ul>
                <li><input type="text" id="username" autocapitalize="off" autocorrect="off"autocomplete="off" placeholder="Username"/></li>
                <li><input type="password" id="password" autocapitalize="off" autocorrect="off"autocomplete="off" placeholder="Password"/></li>
            </ul>
            <ul class="rounded">
                <li class="arrow"><a id="loginButton" href="#">Login</a></li>
            </ul>
            <ul class="rounded">
                <li class="arrow"><a href="${servletUrl}/?interface=default" target="_blank">Use default interface</a></li>
            </ul>
        </div>

        <div id="main">
            <div class="toolbar">
                <h1>MyTunesRSS</h1>
                <a class="back" id="menuLogout" href="#">Logout</a>
            </div>
            <ul class="rounded">
                <li class="arrow"><a class="<%= animation %>" id="menuPlaylists" href="#playlists">Playlists</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albumsindex">Albums</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artistsindex">Artists</a></li>
                <li class="arrow"><a class="<%= animation %>" id="menuGenres" href="#genres">Genres</a></li>
            </ul>
            <ul class="rounded">
                <li><input type="text" id="searchTerm" autocapitalize="off" autocorrect="off"autocomplete="off" placeholder="enter search terms"/></li>
                <li class="arrow"><a id="menuSearch" href="#">Search</a></li>
            </ul>
        </div>

        <div id="playlists">
            <div class="toolbar">
                <h1>Playlists</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge"></ul>
        </div>

        <div id="albumsindex">
            <div class="toolbar">
                <h1>Albums</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge">
                <li class="arrow"><a class="<%= animation %>" href="#albums">0 - 9</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">A - C</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">D - F</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">G - I</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">J - L</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">M - O</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">P - R</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">S - U</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#albums">W - Z</a></li>
            </ul>
        </div>

        <div id="artistsindex">
            <div class="toolbar">
                <h1>Artists</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge">
                <li class="arrow"><a class="<%= animation %>" href="#artists">0 - 9</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">A - C</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">D - F</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">G - I</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">J - L</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">M - O</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">P - R</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">S - U</a></li>
                <li class="arrow"><a class="<%= animation %>" href="#artists">W - Z</a></li>
            </ul>
        </div>

        <div id="albums">
            <div class="toolbar">
                <h1>Albums</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge"></ul>
        </div>

        <div id="artists">
            <div class="toolbar">
                <h1>Artists</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge"></ul>
        </div>

        <div id="genres">
            <div class="toolbar">
                <h1>Genres</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge"></ul>
        </div>

        <div id="tracklist">
            <div class="toolbar">
                <h1>Tracks</h1>
                <a class="back" href="#">Back</a>
            </div>
            <ul class="edgetoedge"></ul>
        </div>

        <div id="jukebox">
            <div class="toolbar">
                <h1>Jukebox</h1>
                <a class="back" href="#">Back</a>
            </div>
            <div id="qtPlugin" style="padding-top: 60px"></div>
        </div>

    </body>

</html>
