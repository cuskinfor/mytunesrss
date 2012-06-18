/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("editplaylist")
public class EditPlaylistResource extends RestResource {

    public static final String KEY_EDIT_PLAYLIST = "playlist";
    public static final String KEY_EDIT_PLAYLIST_TRACKS = "playlistContent";

    /**
     * Get the currently edited playlist from the session.
     *
     * @return A playlist.
     * @throws SQLException
     */
    @GET
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation getPlaylist() throws SQLException {
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }

    /**
     * Get the list of tracks of the currently edited playlist from the session.
     *
     * @param from  Index of first track to return.
     * @param count Maximum number of tracks to return.
     * @return A list of tracks.
     * @throws SQLException
     */
    @GET
    @Path("tracks")
    @Produces("application/json")
    @GZIP
    public List<TrackRepresentation> getPlaylistTracks(
            @QueryParam("from") @DefaultValue("0") int from,
            @QueryParam("count") @DefaultValue("0") int count
    ) throws SQLException {
        List<Track> playlistTracks = (List<Track>) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        if (from >= 0 && from < playlistTracks.size()) {
            return toTrackRepresentations(MyTunesRssUtils.getSubList(playlistTracks, from, count));
        }
        return Collections.emptyList();
    }

    /**
     * Add tracks to the currently edited playlist.
     *
     * @param track       IDs of tracks to add.
     * @param album       Album names to add (all tracks of the album).
     * @param albumArtist Album artist of albums to add.
     * @param artist      Artist names to add (all tracks of the artists).
     * @param genre       Genres to add (all tracks of the genres).
     * @param playlist    IDs of playlists to add (all tracks of the playlists).
     * @return The playlist after adding the tracks.
     * @throws SQLException
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation addTracks(
            @FormParam("track") String[] track,
            @FormParam("album") String[] album,
            @FormParam("albumArtist") String[] albumArtist,
            @FormParam("artist") String[] artist,
            @FormParam("genre") String[] genre,
            @FormParam("playlist") String[] playlist
    ) throws SQLException {
        if (track != null && track.length > 0) {
            addTracks(FindTrackQuery.getForIds(track));
        }
        if (album != null && album.length > 0) {
            addTracks(FindTrackQuery.getForAlbum(getAuthUser(), album, albumArtist, SortOrder.KeepOrder));
        }
        if (artist != null && artist.length > 0) {
            addTracks(FindTrackQuery.getForArtist(getAuthUser(), artist, SortOrder.KeepOrder));
        }
        if (genre != null && genre.length > 0) {
            addTracks(FindTrackQuery.getForGenre(getAuthUser(), genre, SortOrder.KeepOrder));
        }
        if (playlist != null && playlist.length > 0) {
            for (String eachPlaylist : playlist) {
                addTracks(new FindPlaylistTracksQuery(getAuthUser(), eachPlaylist, SortOrder.KeepOrder));
            }
        }
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }

    /**
     * Delete tracks from the currently edited playlist.
     *
     * @param track       IDs of tracks to delete.
     * @param album       Album names to delete (all tracks of the album).
     * @param albumArtist Album artist of albums to delete.
     * @param artist      Artist names to delete (all tracks of the artists).
     * @param genre       Genres to delete (all tracks of the genres).
     * @param playlist    IDs of playlists to delete (all tracks of the playlists).
     * @return The playlist after deleting the tracks.
     * @throws SQLException
     */
    @DELETE
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation removeTracks(
            @FormParam("track") String[] track,
            @FormParam("album") String[] album,
            @FormParam("albumArtist") String[] albumArtist,
            @FormParam("artist") String[] artist,
            @FormParam("genre") String[] genre,
            @FormParam("playlist") String[] playlist
    ) throws SQLException {
        if (track != null && track.length > 0) {
            removeTracks(FindTrackQuery.getForIds(track));
        }
        if (album != null && album.length > 0) {
            removeTracks(FindTrackQuery.getForAlbum(getAuthUser(), album, albumArtist, SortOrder.KeepOrder));
        }
        if (artist != null && artist.length > 0) {
            removeTracks(FindTrackQuery.getForArtist(getAuthUser(), artist, SortOrder.KeepOrder));
        }
        if (genre != null && genre.length > 0) {
            removeTracks(FindTrackQuery.getForGenre(getAuthUser(), genre, SortOrder.KeepOrder));
        }
        if (playlist != null && playlist.length > 0) {
            for (String eachPlaylist : playlist) {
                removeTracks(new FindPlaylistTracksQuery(getAuthUser(), eachPlaylist, SortOrder.KeepOrder));
            }
        }
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }

    private void addTracks(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException {
        Playlist playlist = (Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = new LinkedHashSet<Track>((Collection<Track>) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS));
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        playlistTracks.addAll(tracks);
        myRequest.getSession().setAttribute(KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<Track>(playlistTracks));
        playlist.setTrackCount(playlistTracks.size());
    }

    private void removeTracks(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException {
        Playlist playlist = (Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = (Collection<Track>) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        List<Track> tracks = query != null ? TransactionFilter.getTransaction().executeQuery(query).getResults() : Collections.<Track>emptyList();
        if (tracks != null && !tracks.isEmpty()) {
            playlistTracks.removeAll(tracks);
            playlist.setTrackCount(playlistTracks.size());
        }
    }

    /**
     * Save the currently edited playlist to the database and finish editing the playlist.
     *
     * @param playlistName Name of the playlist.
     * @param userPrivate  "true" to save as a user private list or "false" to save as a public list.
     * @throws SQLException
     * @throws MyTunesRssRestException
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("save")
    public void savePlaylist(
            @FormParam("name") @NotBlank(message = "NO_PLAYLIST_NAME") String playlistName,
            @FormParam("private") @DefaultValue("false") boolean userPrivate
    ) throws SQLException, MyTunesRssRestException {
        Playlist playlist = (Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = (Collection<Track>) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(getAuthUser().getName(), userPrivate);
        statement.setId(playlist.getId());
        statement.setName(playlistName);
        statement.setUpdate(StringUtils.isNotEmpty(playlist.getId()));
        List<String> trackIds = new ArrayList<String>(playlistTracks.size());
        for (Track track : playlistTracks) {
            trackIds.add(track.getId());
        }
        statement.setTrackIds(trackIds);
        TransactionFilter.getTransaction().executeStatement(statement);
        myRequest.getSession().removeAttribute(KEY_EDIT_PLAYLIST);
        myRequest.getSession().removeAttribute(KEY_EDIT_PLAYLIST_TRACKS);
    }

    /**
     * Cancel editing a playlist, all changes are lost.
     */
    @POST
    @Path("cancel")
    public void cancelPlaylist() {
        myRequest.getSession().removeAttribute(KEY_EDIT_PLAYLIST);
        myRequest.getSession().removeAttribute(KEY_EDIT_PLAYLIST_TRACKS);
    }

    /**
     * Move tracks in the playlist to another position.
     *
     * @param first  Index of first track to move (0-based).
     * @param count  Number of tracks to move.
     * @param offset Offset to move, can be positive to move downwards or negative to move upwards.
     */
    @POST
    @Path("move")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation moveTracks(
            @FormParam("from") @DefaultValue("0") int first,
            @FormParam("count") @DefaultValue("0") int count,
            @FormParam("offset") @DefaultValue("0") int offset
    ) {
        MyTunesRssWebUtils.movePlaylistTracks((List<Track>) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS), first, count, offset);
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }
}
