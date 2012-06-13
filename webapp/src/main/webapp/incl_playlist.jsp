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
	    <a id="linkCancel" class="close" href="${servletUrl}/cancelCreatePlaylist/${auth}/backUrl=${mtfn:encode64(backUrl)}" title="<fmt:message key="doCancel" />"><fmt:message key="doCancel" /></a>
		<a id="linkFinish" class="finish" href="${servletUrl}/showResource/${auth}/<mt:encrypt key="${encryptionKey}">resource=EditPlaylist</mt:encrypt>/backUrl=${mtfn:encode64(backUrl)}" title="<fmt:message key="finishPlaylist" />"><fmt:message key="finishPlaylist" /></a>
		<span>
			<strong>${cwfn:choose (empty editPlaylistName, newPlaylistName, editPlaylistName)}</strong>
			- <fmt:message key="playlistTrackCount" />:
			<span id="editPlaylistTrackCount">${editPlaylistTrackCount}</span>
		</span>
    </div>
    <div class="playlistbottom"></div>
</c:if>

<script type="text/javascript">
    function addToPlaylist(artists, genres, tracks) {
        if (artists != null && artists.length > 0 && artists[0] != '') {
            addArtistsToPlaylist(artists);
        } else if (genres != null && genres.length > 0 && genres[0] != '') {
            addGenresToPlaylist(genres);
        } else {
            addTracksToPlaylist(tracks);
        }
    }
    function addTracksToPlaylistInternal(params) {
        var playlist  = EditPlaylistResource.addTracks(params);
        $jQ("#editPlaylistTrackCount").html(playlist.trackCount);
    }
    function addAlbumsToPlaylist(albums, albumArtists) {
        addTracksToPlaylistInternal({
            album : albums,
            albumArtist : albumArtists
        });
    }
    function addArtistsToPlaylist(artists) {
        addTracksToPlaylistInternal({
            artist : artists
            // fullAlbums
        });
    }
    function addGenresToPlaylist(genres) {
        addTracksToPlaylistInternal({
            genre : genres
            // fullAlbums
        });
    }
    function addTracksToPlaylist(tracks) {
        addTracksToPlaylistInternal({
            track : tracks
        });
    }
    function addPlaylistTracksToPlaylist(playlist) {
        addTracksToPlaylistInternal({
            playlist : playlist
            // fullAlbums
        });
    }
</script>
