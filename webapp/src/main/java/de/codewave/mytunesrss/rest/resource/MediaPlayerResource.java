/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.mediarenderercontrol.MediaRendererController;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.MediaPlayerRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Media player operations.
 */
@ValidateRequest
@Path("mediaplayer")
@RequiredUserPermissions({UserPermission.RemoteControl})
public class MediaPlayerResource extends RestResource {

    public enum Action {
        PLAY(), PAUSE(), STOP(), SEEK(), SHUFFLE(), NEXT(), PREVIOUS(), TOGGLE_PLAY_PAUSE()
    }

    private MediaRendererController getController() {
        return MediaRendererController.getInstance();
    }

    /**
     * Replace the current playlists with the specified tracks.
     *
     * @param playlist A playlist ID (all tracks of the playlist will be added).
     * @param album An album name (all tracks of the album will be added).
     * @param albumArtist An album artist name to exactly specify the album.
     * @param artist An artist name (all tracks of the artist will be added).
     * @param genre A genre name (all tracks if the genre will be added).
     * @param track An individual track ID to add.
     * @param tracklist A comma separated list of individual track IDs to add.
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
            @FormParam("track") String track,
            @FormParam("tracklist") String tracklist
    ) throws Exception {
        if (StringUtils.isNotBlank(playlist)) {
            getController().loadPlaylist(MyTunesRssWebUtils.getAuthUser(request), playlist);
        } else if (StringUtils.isNotBlank(album)) {
            getController().loadAlbum(MyTunesRssWebUtils.getAuthUser(request), album, albumArtist);
        } else if (StringUtils.isNotBlank(artist)) {
            getController().loadArtist(MyTunesRssWebUtils.getAuthUser(request), artist);
        } else if (StringUtils.isNotBlank(genre)) {
            getController().loadGenre(MyTunesRssWebUtils.getAuthUser(request), genre);
        } else if (StringUtils.isNotBlank(tracklist)) {
            getController().loadTracks(MyTunesRssWebUtils.getAuthUser(request), StringUtils.split(tracklist, ","));
        } else if (StringUtils.isNotBlank(track)) {
            getController().loadTracks(MyTunesRssWebUtils.getAuthUser(request), new String[] {track});
        } else {
            throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "MISSING_TRACK_IDS");
        }
        return toTrackRepresentations(uriInfo, request, getController().getPlaylist());
    }

    /**
     * Add tracks to the current playlist.
     *
     * @param playlist A playlist ID (all tracks of the playlist will be added).
     * @param album An album name (all tracks of the album will be added).
     * @param albumArtist An album artist name to exactly specify the album.
     * @param artist An artist name (all tracks of the artist will be added).
     * @param genre A genre name (all tracks if the genre will be added).
     * @param track An individual track ID to add.
     * @param tracklist A comma separated list of individual track IDs to add.
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
            @FormParam("playlist") String playlist,
            @FormParam("album") String album,
            @FormParam("albumArtist") String albumArtist,
            @FormParam("artist") String artist,
            @FormParam("genre") String genre,
            @FormParam("track") String track,
            @FormParam("tracklist") String tracklist,
            @FormParam("autostart") @DefaultValue("false") boolean autostart
    ) throws Exception {
        if (StringUtils.isNotBlank(playlist)) {
            getController().addPlaylist(MyTunesRssWebUtils.getAuthUser(request), playlist, autostart);
        } else if (StringUtils.isNotBlank(album)) {
            getController().addAlbum(MyTunesRssWebUtils.getAuthUser(request), album, albumArtist, autostart);
        } else if (StringUtils.isNotBlank(artist)) {
            getController().addArtist(MyTunesRssWebUtils.getAuthUser(request), artist, autostart);
        } else if (StringUtils.isNotBlank(genre)) {
            getController().addGenre(MyTunesRssWebUtils.getAuthUser(request), genre, autostart);
        } else if (StringUtils.isNotBlank(tracklist)) {
            getController().addTracks(MyTunesRssWebUtils.getAuthUser(request), StringUtils.split(tracklist, ","), autostart);
        } else if (StringUtils.isNotBlank(track)) {
            getController().addTracks(MyTunesRssWebUtils.getAuthUser(request), new String[]{track}, autostart);
        } else {
            throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "MISSING_TRACK_IDS");
        }
        return toTrackRepresentations(uriInfo, request, getController().getPlaylist());
    }

    /**
     * Remove the current playlist and stop playback.
     *
     * @throws Exception
     */
    @DELETE
    @Path("playlist")
    public void clearPlaylist() {
        getController().clearPlaylist();
    }

    /**
     * Set player status.
     *
     * @param volume Volume [0-100].
     * @param fullscreen "true" to activate fullscreen playback of video files or "false" to deactivate fullscreen playback.
     * @param renderer Target media renderer.
     * @param action Controller action (One of "PLAY", "PAUSE", "STOP", "SEEK", "SHUFFLE", "NEXT", "PREVIOUS").
     * @param track Track index (when using action "PLAY").
     * @param seek Seek position (when using action "SEEK").
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
            @FormParam("renderer") String renderer,
            @FormParam("action") Action action,
            @FormParam("track") @DefaultValue("-1") int track,
            @FormParam("seek") Integer seek
    ) {
        if (volume != null) {
            getController().setVolume(volume);
        }
        if (fullscreen != null) {
            getController().setFullScreen(fullscreen);
        }
        if (seek != null) {
            getController().seek(seek);
        }
        if (StringUtils.isNotBlank(renderer)) {
            for (RemoteDevice device : MyTunesRss.UPNP_SERVICE.getMediaRenders()) {
                if (renderer.equals(device.getIdentity().getUdn().getIdentifierString())) {
                    getController().setMediaRenderer(device);
                    break;
                }
            }
        } else if (renderer != null) {
            getController().setMediaRenderer(null);
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
                    getController().play(track, true);
                    break;
                case TOGGLE_PLAY_PAUSE:
                    getController().togglePlayPause(true);
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
                    getController().stop(true);
                    break;
                default:
                    throw new IllegalArgumentException("Illegal action \"" + action + "\".");
            }
        }
        return new MediaPlayerRepresentation(getController().getCurrentTrackInfo(), getController().getPlaylistVersion());
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
    ) {
        return toTrackRepresentations(uriInfo, request, getController().getPlaylist());
    }

    private List<TrackRepresentation> toTrackRepresentations(UriInfo uriInfo, HttpServletRequest request, List<Track> tracks) {
        List<TrackRepresentation> trackRepresentations = new ArrayList<>();
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
    ) {
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
    public MediaPlayerRepresentation getStatus() {
        return new MediaPlayerRepresentation(getController().getCurrentTrackInfo(), getController().getPlaylistVersion());
    }

}
