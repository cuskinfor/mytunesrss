/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("playlist")
public class PlaylistResource extends RestResource {

    @POST
    @Path("edit")
    public Response startEditPaylist() throws SQLException {
        myRequest.getSession().setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST, new Playlist());
        myRequest.getSession().setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<Track>());
        return Response.created(UriBuilder.fromResource(EditPlaylistResource.class).build()).build();
    }

    @POST
    @Path("{playlist}/edit")
    public Response startEditPaylist(
            @PathParam("playlist") @NotBlank(message = "Playlist id must not be blank.") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, playlist, null, true, false));
        if (queryResult.getResultSize() == 0) {
            throw new NotFoundException("Playlist \"" + playlist + "\" not found.");
        }
        List<Track> tracks = new ArrayList<Track>(TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(), playlist, null)).getResults());
        myRequest.getSession().setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST, playlist);
        myRequest.getSession().setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS, tracks);
        return Response.created(UriBuilder.fromResource(EditPlaylistResource.class).path(EditPlaylistResource.class, "getPlaylist").build()).build();
    }

    @GET
    @Path("{playlist}")
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation getPlaylist(
            @PathParam("playlist") @NotBlank(message = "Playlist id must not be blank.") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, playlist, null, true, false));
        return queryResult.getResultSize() == 1 ? toPlaylistRepresentation(queryResult.getResult(1)) : null;
    }

    @GET
    @Path("{playlist}/tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getTracks(
            @PathParam("playlist") @NotBlank(message = "Playlist id must not be blank.") String playlist,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(), playlist, sortOrder));
        return toTrackRepresentations(queryResult.getResults());
    }

    @GET
    @Path("{playlist}/playlists")
    @Produces({"application/json"})
    @GZIP
    public List<PlaylistRepresentation> getPlaylistChildren(
            @PathParam("playlist") @NotBlank(message = "Playlist id must not be blank.") String playlist,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), types, null, playlist, includeHidden, matchingOwner));
        return toPlaylistRepresentations(queryResult.getResults());
    }

    @GET
    @Path("{playlist}/tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("playlist") @NotBlank(message = "Playlist id must not be blank.") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForPlaylistQuery(playlist));
        return queryResult.getResults();
    }

    @POST
    @Path("{playlist}/tags")
    @Consumes("application/x-www-form-urlencoded")
    public void setTags(
            @PathParam("playlist") String playlist,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTracks(playlist), tag));
        }
    }

    private String[] getTracks(String playlist) throws SQLException {
        List<? extends Track> tracks = getTracks(playlist, SortOrder.KeepOrder);
        Set<String> trackIds = new HashSet<String>();
        for (Track track : tracks) {
            trackIds.add(track.getId());
        }
        return trackIds.toArray(new String[trackIds.size()]);
    }

    @DELETE
    @Path("{playlist}/tags")
    @Consumes("application/x-www-form-urlencoded")
    public void deleteTags(
            @PathParam("playlist") String playlist,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTracks(playlist), tag));
        }
    }
}
