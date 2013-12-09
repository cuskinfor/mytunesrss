/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.remotecontrol.NoopRemoteController;
import de.codewave.mytunesrss.remotecontrol.RemoteController;
import de.codewave.mytunesrss.remotecontrol.VlcPlayerRemoteController;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.MediaPlayerRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

@ValidateRequest
@Path("mediaplayer")
@RequiredUserPermissions({UserPermission.RemoteControl})
public class MediaPlayerResource extends RestResource {

    public static enum Action {
        PLAY(), PAUSE(), STOP(), SEEK(), SHUFFLE(), NEXT(), PREVIOUS();
    }

    private RemoteController getController() {
        return MyTunesRss.VLC_PLAYER != null ? new VlcPlayerRemoteController() : new NoopRemoteController();
    }

    /**
     * Replace the current playlists with the specified tracks.
     *
     * @param playlist A playlist ID (all tracks of the playlist will be added).
     * @param album An album name (all tracks of the album will be added).
     * @param albumArtist An album artist name to exactly specify the album.
     * @param artist An artist name (all tracks of the artist will be added).
     * @param genre A genre name (all tracks if the genre will be added).
     * @param tracks A list of individual track IDs to add.
     *
     * @return List of tracks in the current playlist.
     *
     * @throws Exception
     */
    @PUT
    @Path("playlist")
    @Produces("application/json")
    public List<TrackRepresentation> setPlaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @FormParam("playlist") String playlist,
            @FormParam("album") String album,
            @FormParam("albumArtist") String albumArtist,
            @FormParam("artist") String artist,
            @FormParam("genre") String genre,
            @FormParam("track") String[] tracks
    ) throws Exception {
        if (StringUtils.isNotBlank(playlist)) {
            getController().loadPlaylist(MyTunesRssWebUtils.getAuthUser(request), playlist);
        } else if (StringUtils.isNotBlank(album)) {
            getController().loadAlbum(MyTunesRssWebUtils.getAuthUser(request), album, albumArtist);
        } else if (StringUtils.isNotBlank(artist)) {
            getController().loadArtist(MyTunesRssWebUtils.getAuthUser(request), artist, false);
        } else if (StringUtils.isNotBlank(genre)) {
            getController().loadGenre(MyTunesRssWebUtils.getAuthUser(request), genre);
        } else if (tracks != null && tracks.length > 0) {
            getController().loadTracks(tracks);
        }
        return toTrackRepresentations(uriInfo, request, getController().getPlaylist());
    }

    /**
     * Add tracks to the current paylist.
     *
     * @param tracks List of track IDs to add.
     * @param autostart Start playback after adding the tracks if not currently playing.
     *
     * @return List of tracks in the current playlist.
     *
     * @throws Exception
     */
    @POST
    @Path("playlist")
    @Produces("application/json")
    public List<TrackRepresentation> addToPlaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @FormParam("track") String[] tracks,
            @FormParam("autostart") @DefaultValue("false") boolean autostart
    ) throws Exception {
        if (tracks == null || tracks.length == 0) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "MISSING_TRACK_IDS");
        }
        getController().addTracks(tracks, autostart);
        return toTrackRepresentations(uriInfo, request, getController().getPlaylist());
    }

    /**
     * Remove the current playlist and stop playback.
     *
     * @throws Exception
     */
    @DELETE
    @Path("playlist")
    public void clearPlaylist() throws Exception {
        getController().clearPlaylist();
    }

    /**
     * Set player status.
     *
     * @param volume Volume [0-100].
     * @param fullscreen "true" to activate fullscreen playback of video files or "false" to decative fullscreen playback.
     * @param targets List of airtunes targets.
     * @param action Controller action (One of "PLAY", "PAUSE", "STOP", "SEEK", "SHUFFLE", "NEXT", "PREVIOUS").
     * @param track Track index (when using action "PLAY").
     * @param seek Seek position (when usin action "SEEK").
     *
     * @return The current media player status.
     *
     * @throws Exception
     */
    @POST
    @Produces("application/json")
    public MediaPlayerRepresentation setStatus(
            @FormParam("volume") Integer volume,
            @FormParam("fullscreen") Boolean fullscreen,
            @FormParam("airtunes") String[] targets,
            @FormParam("action") Action action,
            @FormParam("track") @DefaultValue("-1") int track,
            @FormParam("seek") Integer seek
    ) throws Exception {
        if (volume != null) {
            getController().setVolume(volume);
        }
        if (fullscreen != null) {
            getController().setFullScreen(fullscreen);
        }
        if (seek != null) {
            getController().seek(seek);
        }
        if (targets != null && targets.length > 0) {
            getController().setAirtunesTargets(targets);
        }
        if (action != null) {
            switch (action) {
                case NEXT:
                    getController().next();
                    break;
                case PAUSE:
                    getController().pause();
                    break;
                case PLAY:
                    getController().play(track);
                    break;
                case PREVIOUS:
                    getController().prev();
                    break;
                case SEEK:
                    if (seek == null) {
                        throw new BadRequestException("Missing seek value.");
                    }
                    getController().seek(seek);
                    break;
                case SHUFFLE:
                    getController().shuffle();
                    break;
                case STOP:
                    getController().stop();
                    break;
                default:
                    throw new IllegalArgumentException("Illegal action \"" + action + "\".");
            }
        }
        return new MediaPlayerRepresentation(getController().getCurrentTrackInfo());
    }

    /**
     * Get the current playlist.
     *
     * @return List of tracks in the current playlist.
     *
     * @throws Exception
     */
    @GET
    @Path("playlist")
    @Produces("application/json")
    public List<TrackRepresentation> getPlaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) throws Exception {
        return toTrackRepresentations(uriInfo, request, getController().getPlaylist());
    }

    private List<TrackRepresentation> toTrackRepresentations(UriInfo uriInfo, HttpServletRequest request, List<Track> tracks) {
        List<TrackRepresentation> trackRepresentations = new ArrayList<TrackRepresentation>();
        for (Track track : tracks) {
            trackRepresentations.add(toTrackRepresentation(uriInfo, request, track));
        }
        return trackRepresentations;
    }

    /**
     * Get information about a certain track.
     *
     * @param index Index of the track in the playlist.
     *
     * @return Track information.
     *
     * @throws Exception
     */
    @GET
    @Path("playlist/track/{index}")
    @Produces("application/json")
    public TrackRepresentation getTrack(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("index") int index
    ) throws Exception {
        return toTrackRepresentation(uriInfo, request, getController().getTrack(index));
    }

    /**
     * Get the current media player status.
     *
     * @return The current media player status.
     *
     * @throws Exception
     */
    @GET
    @Produces("application/json")
    public MediaPlayerRepresentation getStatus() throws Exception {
        return new MediaPlayerRepresentation(getController().getCurrentTrackInfo());
    }

}
