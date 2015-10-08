/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.AlbumRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultSetType;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;

/**
 * Album operations.
 */
@ValidateRequest
@Path("artist/{artist}/album/{album}")
@RequiredUserPermissions({UserPermission.Audio})
public class AlbumResource extends RestResource {

    /**
     * Get the representation of a single album specified by albuma artist and album name.
     *
     * @param artist The album artist.
     * @param album The album name.
     *
     * @return The representation of the album.
     *
     * @throws SQLException
     */
    @GET
    @Produces({"application/json"})
    @GZIP
    public AlbumRepresentation getAlbum(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("artist") String artist,
            @PathParam("album") String album
    ) throws SQLException {
        FindAlbumQuery findAlbumQuery = new FindAlbumQuery(MyTunesRssWebUtils.getAuthUser(request), album, artist, true, null, -1, -1, -1, true, false, FindAlbumQuery.AlbumType.ALL);
        findAlbumQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(findAlbumQuery);
        for (Album result = queryResult.nextResult(); result != null; result = queryResult.nextResult()) {
            if (result.getName().equals(album)) {
                return toAlbumRepresentation(uriInfo, request, result);
            }
        }
        return null;
    }

    /**
     * Get a list of tracks of an album specified by album artist and album name.
     *
     * @param artist The album artist.
     * @param album The album name.
     * @param sortOrder Sort order of the results (One of "Album", "Artist", "KeepOrder").
     *
     * @return List of track representations.
     *
     * @throws SQLException
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.TrackRepresentation>
     */
    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public Iterable<TrackRepresentation> getAlbumTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("artist") String artist,
            @PathParam("album") String album,
            @QueryParam("sort") @DefaultValue("Album") SortOrder sortOrder
    ) throws SQLException {
        FindTrackQuery findTrackQuery = FindTrackQuery.getForAlbum(MyTunesRssWebUtils.getAuthUser(request), new String[]{album}, new String[]{artist}, sortOrder);
        findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        return toTrackRepresentations(uriInfo, request, queryResult);
    }

}
