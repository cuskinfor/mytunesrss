/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ValidateRequest
@Path("playlist")
public class PlaylistResource extends RestResource {

    @POST
    @Path("edit")
    public Response startEditNewPlaylist() throws SQLException {
        myRequest.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST, new Playlist());
        myRequest.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<Track>());
        return Response.created(UriBuilder.fromResource(EditPlaylistResource.class).build()).build();
    }

    @POST
    @Path("{playlist}/edit")
    public Response startEditPaylist(
            @Context UriInfo uriInfo,
            @PathParam("playlist") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, playlist, null, true, false));
        if (queryResult.getResultSize() == 0) {
            throw new NotFoundException("Playlist \"" + playlist + "\" not found.");
        }
        List<Track> tracks = new ArrayList<Track>(TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(), playlist, null)).getResults());
        myRequest.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST, queryResult.nextResult());
        myRequest.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS, tracks);
        return Response.created(uriInfo.getBaseUriBuilder().path(EditPlaylistResource.class).build()).build();
    }

    @GET
    @Path("{playlist}")
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation getPlaylist(
            @PathParam("playlist") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), null, playlist, null, true, false));
        return queryResult.getResultSize() == 1 ? toPlaylistRepresentation(queryResult.getResult(1)) : null;
    }

    @GET
    @Path("{playlist}/tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getTracks(
            @PathParam("playlist") String playlist,
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
            @PathParam("playlist") String playlist,
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
            @PathParam("playlist") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForPlaylistQuery(playlist));
        return queryResult.getResults();
    }

    @PUT
    @Path("{playlist}/tag/{tag}")
    @Produces({"application/json"})
    public List<String> setTag(
            @PathParam("playlist") String playlist,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTrackIds(playlist), tag));
        return getTags(playlist);
    }

    private String[] getTrackIds(String playlist) throws SQLException {
        List<TrackRepresentation> tracks = getTracks(playlist, SortOrder.KeepOrder);
        Set<String> trackIds = new HashSet<String>();
        for (Track track : tracks) {
            trackIds.add(track.getId());
        }
        return trackIds.toArray(new String[trackIds.size()]);
    }

    @DELETE
    @Path("{playlist}/tag/{tag}")
    @Produces({"application/json"})
    public List<String> deleteTag(
            @PathParam("playlist") String playlist,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTrackIds(playlist), tag));
        return getTags(playlist);
    }
}
