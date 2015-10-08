/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.AlbumRepresentation;
import de.codewave.mytunesrss.rest.representation.PartialListRepresentation;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.*;

/**
 * Playlist editing operations.
 */
@ValidateRequest
@Path("editplaylist")
@RequiredUserPermissions({UserPermission.CreatePlaylists})
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
    public PlaylistRepresentation getPlaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) {
        return toPlaylistRepresentation(uriInfo, request, (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST));
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
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("from") @DefaultValue("0") int from,
            @QueryParam("count") @DefaultValue("0") int count
    ) {
        List<Track> playlistTracks = (List<Track>) request.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        List<TrackRepresentation> trackRepresentations = new ArrayList<>();
        if (from >= 0 && from < playlistTracks.size()) {
            for (int i = from; i < from + count && i < playlistTracks.size(); i++) {
                trackRepresentations.add(toTrackRepresentation(uriInfo, request, playlistTracks.get(i)));
            }
        }
        return trackRepresentations;
    }

    /**
     * Get the list of albums of the currently edited playlist from the session. The track count of the albums reflects the number
     * of tracks of the album in the playlist, not the total track count of the album in the library.
     *
     * @param from  Index of first album to return.
     * @param count Maximum number of albums to return.
     * @return List of albums and the total album count. Each item in the list is an albumRepresentation.
     * @throws SQLException
     */
    @GET
    @Path("albums")
    @Produces("application/json")
    @GZIP
    public PartialListRepresentation<AlbumRepresentation> getPlaylistAlbums(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("from") @DefaultValue("0") int from,
            @QueryParam("count") @DefaultValue("0") int count
    ) {
        List<Track> playlistTracks = (List<Track>) request.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        List<Album> playlistAlbums = new ArrayList<>();
        for (Track track : playlistTracks) {
            Album album = null;
            for (Album existingAlbum : playlistAlbums) {
                if (existingAlbum.getName().equals(track.getAlbum()) && existingAlbum.getArtist().equals(track.getAlbumArtist())) {
                    album = existingAlbum;
                    break;
                }
            }
            if (album != null) {
                // album already in list, edit it
                album.setTrackCount(album.getTrackCount() + 1);
                if (album.getImageHash() == null) {
                    album.setImageHash(track.getImageHash());
                }
            } else {
                // album not yet in list, add a new one
                album = new Album();
                album.setArtist(track.getAlbumArtist());
                album.setTrackCount(1);
                album.setImageHash(track.getImageHash());
                album.setName(track.getAlbum());
                album.setNaturalSortName(track.getAlbum());
                album.setYear(track.getYear());
                playlistAlbums.add(album);
            }
        }
        Collections.sort(playlistAlbums, new Comparator<Album>() {
            @Override
            public int compare(Album a1, Album a2) {
                return StringUtils.trimToEmpty(a1.getName()).compareTo(StringUtils.trimToEmpty(a2.getName()));
            }
        });
        List<AlbumRepresentation> representations = new ArrayList<>();
        if (from >= 0 && from < playlistTracks.size()) {
            for (int i = from; i < from + count && i < playlistTracks.size(); i++) {
                representations.add(toAlbumRepresentation(uriInfo, request, playlistAlbums.get(i)));
            }
        }
        return new PartialListRepresentation<>(representations, playlistAlbums.size());
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
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation addTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @FormParam("track") String[] track,
            @FormParam("album") String[] album,
            @FormParam("albumArtist") String[] albumArtist,
            @FormParam("artist") String[] artist,
            @FormParam("genre") String[] genre,
            @FormParam("playlist") String[] playlist
    ) throws SQLException {
        if (track != null && track.length > 0) {
            addTracks(request, FindTrackQuery.getForIds(track));
        }
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (album != null && album.length > 0) {
            addTracks(request, FindTrackQuery.getForAlbum(user, album, albumArtist, SortOrder.KeepOrder));
        }
        if (artist != null && artist.length > 0) {
            addTracks(request, FindTrackQuery.getForArtist(user, artist, SortOrder.KeepOrder));
        }
        if (genre != null && genre.length > 0) {
            addTracks(request, FindTrackQuery.getForGenre(user, genre, SortOrder.KeepOrder));
        }
        if (playlist != null && playlist.length > 0) {
            for (String eachPlaylist : playlist) {
                addTracks(request, new FindPlaylistTracksQuery(user, eachPlaylist, SortOrder.KeepOrder));
            }
        }
        return toPlaylistRepresentation(uriInfo, request, (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }

    /**
     * Delete track from the currently edited playlist.
     *
     * @param track       IDs of tracks to delete.
     *
     * @return The playlist after deleting the tracks.
     *
     * @throws SQLException
     */
    @DELETE
    @Path("track/{track}")
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation removeTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("track") String track
    ) throws SQLException {
        removeTracks(request, FindTrackQuery.getForIds(new String[] {track}));
        return toPlaylistRepresentation(uriInfo, request, (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }

    /**
     * Delete all tracks of an album from the currently edited playlist.
     *
     * @param artist    Album artist.
     * @param album     Album name.
     *
     * @return The playlist after deleting the tracks.
     *
     * @throws SQLException
     */
    @DELETE
    @Path("artist/{artist}/album/{album}")
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation removeAlbumTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("artist") String artist,
            @PathParam("album") String album
    ) throws SQLException {
        removeTracks(request, FindTrackQuery.getForAlbum(MyTunesRssWebUtils.getAuthUser(request), new String[]{album}, new String[]{artist}, SortOrder.KeepOrder));
        return toPlaylistRepresentation(uriInfo, request, (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }

    private void addTracks(HttpServletRequest request, DataStoreQuery<QueryResult<Track>> query) throws SQLException {
        Playlist playlist = (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = new LinkedHashSet<>((Collection<Track>) request.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS));
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        playlistTracks.addAll(tracks);
        request.getSession().setAttribute(KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<>(playlistTracks));
        playlist.setTrackCount(playlistTracks.size());
    }

    private void removeTracks(HttpServletRequest request, DataStoreQuery<QueryResult<Track>> query) throws SQLException {
        Playlist playlist = (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = (Collection<Track>) request.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
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
     *
     * @return URI of the saved playlist is returned in the "Location" HTTP response header.
     *
     * @throws SQLException
     * @throws MyTunesRssRestException
     */
    @POST
    @Path("save")
    @Produces("text/plain")
    public Response savePlaylist(
            @Context HttpServletRequest request,
            @FormParam("name") @NotBlank(message = "NO_PLAYLIST_NAME") String playlistName,
            @FormParam("private") @DefaultValue("false") boolean userPrivate
    ) throws SQLException {
        Playlist playlist = (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = (Collection<Track>) request.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(MyTunesRssWebUtils.getAuthUser(request).getName(), userPrivate);
        statement.setId(playlist.getId());
        statement.setName(playlistName);
        statement.setUpdate(StringUtils.isNotEmpty(playlist.getId()));
        List<String> trackIds = new ArrayList<>(playlistTracks.size());
        for (Track track : playlistTracks) {
            trackIds.add(track.getId());
        }
        statement.setTrackIds(trackIds);
        TransactionFilter.getTransaction().executeStatement(statement);
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.MEDIA_SERVER_UPDATE));
        request.getSession().removeAttribute(KEY_EDIT_PLAYLIST);
        request.getSession().removeAttribute(KEY_EDIT_PLAYLIST_TRACKS);
        return Response.created(PlaylistResource.GET_PLAYLIST_URI_BUILDER.clone().build(statement.getId())).build();
    }

    /**
     * Cancel editing a playlist, all changes are lost.
     */
    @POST
    @Path("cancel")
    public void cancelPlaylist(
            @Context HttpServletRequest request
    ) {
        request.getSession().removeAttribute(KEY_EDIT_PLAYLIST);
        request.getSession().removeAttribute(KEY_EDIT_PLAYLIST_TRACKS);
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
    @Produces("application/json")
    @GZIP
    public PlaylistRepresentation moveTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @FormParam("from") @DefaultValue("0") int first,
            @FormParam("count") @DefaultValue("0") int count,
            @FormParam("offset") @DefaultValue("0") int offset
    ) {
        MyTunesRssWebUtils.movePlaylistTracks((List<Track>) request.getSession().getAttribute(KEY_EDIT_PLAYLIST_TRACKS), first, count, offset);
        return toPlaylistRepresentation(uriInfo, request, (Playlist) request.getSession().getAttribute(KEY_EDIT_PLAYLIST));
    }
}
