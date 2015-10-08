/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.PlaylistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultSetType;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Playlist operations.
 */
@ValidateRequest
@Path("playlist")
@RequiredUserPermissions({UserPermission.Playlist})
public class PlaylistResource extends RestResource {

    static final UriBuilder GET_PLAYLIST_URI_BUILDER;

    static {
        try {
            GET_PLAYLIST_URI_BUILDER = UriBuilder.
                    fromResource(PlaylistResource.class).
                    path(PlaylistResource.class.getMethod("getPlaylist", UriInfo.class, HttpServletRequest.class, String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find resource method!", e);
        }
    }

    /**
     * Start editing a new, blank playlist.
     *
     * @return URI of the new playlist.
     *
     * @throws SQLException
     */
    @POST
    @Path("edit")
    @Produces("text/plain")
    public Response startEditNewPlaylist(
            @Context HttpServletRequest request
    ) {
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
     * @return URI of the edit-mode playlist is returned in the "Location" HTTP response header.
     *
     * @throws SQLException
     */
    @POST
    @Path("{playlist}/edit")
    @Produces("text/plain")
    public Response startEditPaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist
    ) throws SQLException {
        QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), null, playlist, null, true, false));
        if (queryResult.getResultSize() == 0) {
            throw new NotFoundException("Playlist \"" + playlist + "\" not found.");
        }
        List<Track> tracks = new ArrayList<>(TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(MyTunesRssWebUtils.getAuthUser(request), playlist, null)).getResults());
        request.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST, queryResult.nextResult());
        request.getSession().setAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS, tracks);
        return Response.created(uriInfo.getBaseUriBuilder().path(EditPlaylistResource.class).build()).build();
    }

    /**
     * Refresh a smart playlist owned by the current user. The request fails if the playlist
     * is not a smart playlist or if it is not owned by the current user.
     *
     * @param uriInfo
     * @param playlist The ID of the playlist to refresh.
     *
     * @return The playlist after the refresh has finished.
     *
     * @throws SQLException
     */
    @POST
    @Path("{playlist}/refresh")
    @Produces({"application/json"})
    @GZIP
    public PlaylistRepresentation refreshSmartPlaylist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist
    ) throws SQLException {
        SmartPlaylist smartPlaylist = TransactionFilter.getTransaction().executeQuery(new FindSmartPlaylistQuery(playlist));
        if (smartPlaylist == null) {
            throw new NotFoundException("No smart playlist \"" + playlist + "\" found.");
        }
        if (!MyTunesRssWebUtils.getAuthUser(request).getName().equals(smartPlaylist.getPlaylist().getUserOwner())) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_UNAUTHORIZED, "PLAYLIST_NOT_OWNER");
        }
        Collection<SmartInfo> smartInfos = smartPlaylist.getSmartInfos();
        if (smartInfos != null) {
            TransactionFilter.getTransaction().executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, playlist));
        }
        smartPlaylist = TransactionFilter.getTransaction().executeQuery(new FindSmartPlaylistQuery(playlist));
        return toPlaylistRepresentation(uriInfo, request, smartPlaylist.getPlaylist());
    }

    /**
     * Get the representation of a playlist.
     *
     * @param uriInfo
     * @param request
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
        QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), null, playlist, null, true, false));
        return queryResult.getResultSize() == 1 ? toPlaylistRepresentation(uriInfo, request, queryResult.getResult(0)) : null;
    }

    /**
     * Get the tracks of a playlist.
     *
     * @param uriInfo
     * @param request
     * @param playlist A playlist ID.
     * @param sortOrder Sort order of the results (One of "Album", "Artist", "KeepOrder").
     *
     * @return Tracks of the playlist.
     *
     * @throws SQLException
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.TrackRepresentation>
     */
    @GET
    @Path("{playlist}/tracks")
    @Produces({"application/json"})
    @GZIP
    public Iterable<TrackRepresentation> getTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        FindPlaylistTracksQuery findPlaylistTracksQuery = new FindPlaylistTracksQuery(MyTunesRssWebUtils.getAuthUser(request), playlist, sortOrder);
        findPlaylistTracksQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(findPlaylistTracksQuery);
        return toTrackRepresentations(uriInfo, request, queryResult);
    }

    /**
     * Get children playlist (for iTunes folders).
     *
     * @param uriInfo
     * @param request
     * @param playlist A playlist ID (should be an iTunes folder).
     * @param includeHidden "true" to include hidden playlists which should not be shown in user interfaces.
     * @param matchingOwner "true" to return playlists owned by the currently logged in user only.
     * @param types List of playlist types to return (Possible values: "ITunes", "MyTunes", "M3uFile", "ITunesFolder", "MyTunesSmart", "System").
     *
     * @return List of playlists.
     *
     * @throws SQLException
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.PlaylistRepresentation>
     */
    @GET
    @Path("{playlist}/playlists")
    @Produces({"application/json"})
    @GZIP
    public Iterable<PlaylistRepresentation> getPlaylistChildren(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types
    ) throws SQLException {
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), types, null, playlist, includeHidden, matchingOwner);
        findPlaylistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(findPlaylistQuery);
        return toPlaylistRepresentations(uriInfo, request, queryResult);
    }

    /**
     * Delete a playlist. Only playlists of the currently logged in user can be deleted.
     *
     * @param request
     * @param playlist A playlist ID (should be a MyTunesRSS playlist owned by the current user).
     *
     * @throws SQLException
     */
    @DELETE
    @Path("{playlist}")
    @RequiredUserPermissions({UserPermission.CreatePlaylists})
    public void deletePlaylist(
            @Context HttpServletRequest request,
            @PathParam("playlist") String playlist
    ) throws SQLException {
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(MyTunesRssWebUtils.getAuthUser(request), null, playlist, null, true, true);
        if (TransactionFilter.getTransaction().executeQuery(findPlaylistQuery).getRemainingResults().isEmpty()) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_FORBIDDEN, "PLAYLIST_NOT_OWNER");
        }
        DeletePlaylistStatement deletePlaylistStatement = new DeletePlaylistStatement();
        deletePlaylistStatement.setId(playlist);
        TransactionFilter.getTransaction().executeStatement(deletePlaylistStatement);
    }

}
