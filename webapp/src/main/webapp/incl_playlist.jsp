<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/tags" prefix="mt" %>
<%@ taglib uri="http://www.codewave.de/jsp/functions" prefix="cwfn" %>
<%@ taglib uri="http://www.codewave.de/mytunesrss/jsp/functions" prefix="mtfn" %>

<fmt:message var="newPlaylistName" key="newPlaylistName" />

<c:if test="${stateEditPlaylist}">
    <div class="playlisttop"></div>
    <div class="playlist">
        <a class="close" href="${servletUrl}/cancelCreatePlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}"><img src="${appUrl}/images/cancel.gif" alt=""/></a>
				<a class="finish" href="${servletUrl}/showResource/${auth}/<mt:encrypt key="${encryptionKey}">resource=EditPlaylist</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}"><img src="${appUrl}/images/finish.gif" alt=""/></a>
				<span>
					<strong>${cwfn:choose (empty editPlaylistName, newPlaylistName, editPlaylistName)}</strong>
					- <fmt:message key="playlistTrackCount" /> : <span id="editPlaylistTrackCount">${editPlaylistTrackCount}</span>
				</span>
    </div>
    <div class="playlistbottom"></div>
</c:if>

<script type="text/javascript">
    function addToPlaylist(albums, artists, genres, tracks, fullAlbums) {
        if (albums != null && albums.length > 0 && albums[0] != '') {
            addAlbumsToPlaylist(albums);
        } else if (artists != null && artists.length > 0 && artists[0] != '') {
            addArtistsToPlaylist(artists, fullAlbums);
        } else if (genres != null && genres.length > 0 && genres[0] != '') {
            addGenresToPlaylist(genres, fullAlbums);
        } else {
            addTracksToPlaylist(tracks);
        }
    }
    function addAlbumsToPlaylist(albums) {
        jsonRpc('${servletUrl}', 'EditPlaylistService.addAlbums', [albums], updateEditPlaylistCount, '${remoteApiSessionId}');
    }
    function addArtistsToPlaylist(artists, fullAlbums) {
        jsonRpc('${servletUrl}', 'EditPlaylistService.addArtists', [artists, fullAlbums], updateEditPlaylistCount, '${remoteApiSessionId}');
    }
    function addGenresToPlaylist(genres, fullAlbums) {
        jsonRpc('${servletUrl}', 'EditPlaylistService.addGenres', [genres, fullAlbums], updateEditPlaylistCount, '${remoteApiSessionId}');
    }
    function addTracksToPlaylist(tracks) {
        jsonRpc('${servletUrl}', 'EditPlaylistService.addTracks', [tracks], updateEditPlaylistCount, '${remoteApiSessionId}');
    }
    function updateEditPlaylistCount(result) {
        $jQ("#editPlaylistTrackCount").html(result.count);
    }
</script>
