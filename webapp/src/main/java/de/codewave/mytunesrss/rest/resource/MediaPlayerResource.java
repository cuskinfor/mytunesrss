/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.remote.service.NoopRemoteController;
import de.codewave.mytunesrss.remote.service.RemoteController;
import de.codewave.mytunesrss.remote.service.RemoteTrackInfo;
import de.codewave.mytunesrss.remote.service.VlcPlayerRemoteController;
import de.codewave.mytunesrss.rest.representation.MediaPlayerRepresentation;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.util.List;

@ValidateRequest
@Path("mediaplayer")
public class MediaPlayerResource extends RestResource {

    public static enum Action {
        PLAY(), PAUSE(), STOP(), SEEK(), SHUFFLE(), NEXT(), PREVIOUS();
    }

    private RemoteController getController() {
        return MyTunesRss.VLC_PLAYER != null ? new VlcPlayerRemoteController() : new NoopRemoteController();
    }

    @PUT
    @Path("playlist")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public List<TrackRepresentation> setPlaylist(
            @FormParam("playlist") String playlist,
            @FormParam("album") String album,
            @FormParam("artist") String artist,
            @FormParam("genre") String genre,
            @FormParam("track") String[] tracks
    ) throws Exception {
        if (StringUtils.isNotBlank(playlist)) {
            getController().loadPlaylist(playlist);
        } else if (StringUtils.isNotBlank(album)) {
            getController().loadAlbum(album);
        } else if (StringUtils.isNotBlank(artist)) {
            getController().loadArtist(artist, false);
        } else if (StringUtils.isNotBlank(genre)) {
            getController().loadGenre(genre);
        } else if (tracks != null && tracks.length > 0) {
            getController().loadTracks(tracks);
        }
        return toTrackRepresentations(getController().getPlaylist());
    }

    @POST
    @Path("playlist")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public List<TrackRepresentation> addToPlaylist(
            @FormParam("track") @NotBlank(message = "No tracks specified.") String[] tracks,
            @FormParam("autostart") @DefaultValue("false") boolean autostart
    ) throws Exception {
        getController().addTracks(tracks, autostart);
        return toTrackRepresentations(getController().getPlaylist());
    }

    @DELETE
    @Path("playlist")
    public void clearPlaylist() throws Exception {
        getController().clearPlaylist();
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
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

    @GET
    @Path("playlist")
    @Produces("application/json")
    public List<TrackRepresentation> getPlaylist() throws Exception {
        return toTrackRepresentations(getController().getPlaylist());
    }

    @GET
    @Path("playlist/track/{index}")
    @Produces("application/json")
    public TrackRepresentation getTrack(
            @PathParam("index") int index
    ) throws Exception {
        return toTrackRepresentation(getController().getTrack(index));
    }

    @GET
    @Produces("application/json")
    public MediaPlayerRepresentation getStatus() throws Exception {
        return new MediaPlayerRepresentation(getController().getCurrentTrackInfo());
    }

}
