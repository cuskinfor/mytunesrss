/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
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

    /**
     * Start editing a new, blank playlist.
     *
     * @return URI of the new playlist.
     *
     * @throws SQLException
     */
    @POST
    @Path("edit")
    public Response startEditNewPlaylist(
            @Context HttpServletRequest request
    ) throws SQLException {
        request.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST, new Playlist());
        request.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<Track>());
        return Response.created(UriBuilder.fromResource(EditPlaylistResource.class).build()).build();
    }

    /**
     * Start editing an existing playlist.
     *
     * @param uriInfo
     * @param playlist The ID of the playlist to edit.
     *
     * @return URI of the new playlist.
     *
     * @throws SQLException
     */
    @POST
    @Path("{playlist}/edit")
    public Response startEditPaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), null, playlist, null, true, false));
        if (queryResult.getResultSize() == 0) {
            throw new NotFoundException("Playlist \"" + playlist + "\" not found.");
        }
        List<Track> tracks = new ArrayList<Track>(TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(MyTunesRssWebUtils.getAuthUser(request), playlist, null)).getResults());
        request.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST, queryResult.nextResult());
        request.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS, tracks);
        return Response.created(uriInfo.getBaseUriBuilder().path(EditPlaylistResource.class).build()).build();
    }

    /**
     * Get the representation of a playlist.
     *
     * @param playlist A playlist ID.
     *
     * @return The playlist.
     *
     * @throws SQLException
     */
    @GET
    @Path("{playlist}")
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation getPlaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), null, playlist, null, true, false));
        return queryResult.getResultSize() == 1 ? toPlaylistRepresentation(uriInfo, request, queryResult.getResult(1)) : null;
    }

    /**
     * Get the tracks of a playlist.
     *
     * @param playlist A playlist ID.
     * @param sortOrder Sort order of the results (One of "Album", "Artist", "KeepOrder").
     *
     * @return Tracks of the playlist.
     *
     * @throws SQLException
     */
    @GET
    @Path("{playlist}/tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(MyTunesRssWebUtils.getAuthUser(request), playlist, sortOrder));
        return toTrackRepresentations(uriInfo, request, queryResult.getResults());
    }

    /**
     * Get children playlist (for iTunes folders).
     *
     * @param playlist A playlist ID (should be an iTunes folder).
     * @param includeHidden "true" to include hidden playlists which should not be shown in user interfaces.
     * @param matchingOwner "true" to return playlists owned by the currently logged in user only.
     * @param types List of playlist types to return (Possible values: "ITunes", "MyTunes", "M3uFile", "ITunesFolder", "MyTunesSmart", "Random", "System").
     *
     * @return List of playlists.
     *
     * @throws SQLException
     */
    @GET
    @Path("{playlist}/playlists")
    @Produces({"application/json"})
    @GZIP
    public List<PlaylistRepresentation> getPlaylistChildren(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), types, null, playlist, includeHidden, matchingOwner));
        return toPlaylistRepresentations(uriInfo, request, queryResult.getResults());
    }

    /**
     * Get a list of tags for the playlist, i.e. a list of tags for all tracks. All tags of all tracks of the playlist are returned,
     * i.e. not all tracks have all the tags returned but only at least one track has each of the tags.
     *
     * @param playlist A playlist ID.
     *
     * @return List of all tags.
     *
     * @throws SQLException
     */
    @GET
    @Path("{playlist}/tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("playlist") String playlist
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForPlaylistQuery(playlist));
        return queryResult.getResults();
    }

    /**
     * Set a tag to all tracks of the playlist.
     *
     * @param playlist A playlist ID.
     * @param tag The tag to set.
     *
     * @return List of all tags after adding the specified one.
     *
     * @throws SQLException
     */
    @PUT
    @Path("{playlist}/tag/{tag}")
    @Produces({"application/json"})
    public List<String> setTag(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTrackIds(uriInfo, request, playlist), tag));
        return getTags(playlist);
    }

    private String[] getTrackIds(UriInfo uriInfo, HttpServletRequest request, String playlist) throws SQLException {
        List<TrackRepresentation> tracks = getTracks(uriInfo, request, playlist, SortOrder.KeepOrder);
        Set<String> trackIds = new HashSet<String>();
        for (TrackRepresentation track : tracks) {
            trackIds.add(track.getId());
        }
        return trackIds.toArray(new String[trackIds.size()]);
    }

    /**
     * Delete a tag from all tracks of an playlist.
     *
     * @param playlist A playlist ID.
     * @param tag The tag to delete.
     *
     * @return List of all tags after deleting the specified one.
     *
     * @throws SQLException
     */
    @DELETE
    @Path("{playlist}/tag/{tag}")
    @Produces({"application/json"})
    public List<String> deleteTag(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTrackIds(uriInfo, request, playlist), tag));
        return getTags(playlist);
    }
}
