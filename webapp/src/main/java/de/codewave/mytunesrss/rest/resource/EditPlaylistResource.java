/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("editplaylist")
public class EditPlaylistResource extends RestResource {

    @GET
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation getPlaylist() throws SQLException {
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST));
    }

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getPlaylistTracks(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("count") @DefaultValue("0") int count
    ) throws SQLException {
        List<Track> playlistTracks = (List<Track>) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
        if (first > 0 && first < playlistTracks.size()) {
            return toTrackRepresentations(MyTunesRssUtils.getSubList(playlistTracks, first, count));
        }
        return Collections.emptyList();
    }

    @POST
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation addTracks(
            @QueryParam("track") String[] track,
            @QueryParam("album") String[] album,
            @QueryParam("albumArtist") String[] albumArtist,
            @QueryParam("artist") String[] artist,
            @QueryParam("genre") String[] genre,
            @QueryParam("playlist") String[] playlist
    ) throws SQLException {
        if (track != null && track.length > 0) {
            addTracks(FindTrackQuery.getForIds(track));
        }
        if (album != null && album.length > 0) {
            addTracks(FindTrackQuery.getForAlbum(getAuthUser(), decode(album), decode(albumArtist), SortOrder.KeepOrder));
        }
        if (artist != null && artist.length > 0) {
            addTracks(FindTrackQuery.getForArtist(getAuthUser(), decode(artist), SortOrder.KeepOrder));
        }
        if (genre != null && genre.length > 0) {
            addTracks(FindTrackQuery.getForGenre(getAuthUser(), decode(genre), SortOrder.KeepOrder));
        }
        if (playlist != null && playlist.length > 0) {
            for (String eachPlaylist : playlist) {
                addTracks(new FindPlaylistTracksQuery(getAuthUser(), eachPlaylist, SortOrder.KeepOrder));
            }
        }
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST));
    }

    @DELETE
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation removeTracks(
            @QueryParam("track") String[] track,
            @QueryParam("album") String[] album,
            @QueryParam("albumArtist") String[] albumArtist,
            @QueryParam("artist") String[] artist,
            @QueryParam("genre") String[] genre,
            @QueryParam("playlist") String[] playlist
    ) throws SQLException {
        if (track != null && track.length > 0) {
            removeTracks(FindTrackQuery.getForIds(track));
        }
        if (album != null && album.length > 0) {
            removeTracks(FindTrackQuery.getForAlbum(getAuthUser(), decode(album), decode(albumArtist), SortOrder.KeepOrder));
        }
        if (artist != null && artist.length > 0) {
            removeTracks(FindTrackQuery.getForArtist(getAuthUser(), decode(artist), SortOrder.KeepOrder));
        }
        if (genre != null && genre.length > 0) {
            removeTracks(FindTrackQuery.getForGenre(getAuthUser(), decode(genre), SortOrder.KeepOrder));
        }
        if (playlist != null && playlist.length > 0) {
            for (String eachPlaylist : playlist) {
                removeTracks(new FindPlaylistTracksQuery(getAuthUser(), eachPlaylist, SortOrder.KeepOrder));
            }
        }
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST));
    }

    private String[] decode(String[] encoded) {
        if (encoded == null) {
            return null;
        }
        String[] decoded = new String[encoded.length];
        for (int i = 0; i < encoded.length; i++) {
            decoded[i] = MyTunesRssBase64Utils.decodeToString(encoded[i]);
        }
        return decoded;
    }

    private void addTracks(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException {
        Playlist playlist = (Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = new LinkedHashSet<Track>((Collection<Track>) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS));
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        playlistTracks.addAll(tracks);
        myRequest.getSession().setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<Track>(playlistTracks));
        playlist.setTrackCount(playlistTracks.size());
    }

    private void removeTracks(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException {
        Playlist playlist = (Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
        Collection<Track> playlistTracks = (Collection<Track>) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
        List<Track> tracks = query != null ? TransactionFilter.getTransaction().executeQuery(query).getResults() : Collections.<Track>emptyList();
        if (tracks != null && !tracks.isEmpty()) {
            playlistTracks.removeAll(tracks);
            playlist.setTrackCount(playlistTracks.size());
        }
    }

    @POST
    @Path("save")
    public void savePlaylist(
            @QueryParam("name") String playlistName,
            @QueryParam("private") Boolean userPrivate
    ) throws SQLException {
        Playlist playlist = (Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
        if (StringUtils.isNotBlank(playlistName)) {
            playlist.setName(playlistName);
        }
        if (StringUtils.isBlank(playlist.getName())) {
            throw new BadRequestException("Playlist has no name and no name specified in request.");
        }
        Collection<Track> playlistTracks = (Collection<Track>) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
        if (userPrivate != null) {
            playlist.setUserPrivate(userPrivate);
        }
        playlist.setUserOwner(getAuthUser().getName());
        SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(getAuthUser().getName(), userPrivate);
        statement.setId(playlist.getId());
        statement.setName(playlist.getName());
        statement.setUserPrivate(playlist.isUserPrivate());
        statement.setUpdate(StringUtils.isNotEmpty(playlist.getId()));
        List<String> trackIds = new ArrayList<String>(playlistTracks.size());
        for (Track track : playlistTracks) {
            trackIds.add(track.getId());
        }
        statement.setTrackIds(trackIds);
        TransactionFilter.getTransaction().executeStatement(statement);
        myRequest.getSession().removeAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
        myRequest.getSession().removeAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
    }

    @POST
    @Path("cancel")
    public void cancelPlaylist() {
        myRequest.getSession().removeAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
        myRequest.getSession().removeAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
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
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation moveTracks(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("count") @DefaultValue("0") int count,
            @QueryParam("offset") @DefaultValue("0") int offset
    ) {
        MyTunesRssWebUtils.movePlaylistTracks((List<Track>) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS), first, count, offset);
        return toPlaylistRepresentation((Playlist) myRequest.getSession().getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST));
    }
}
