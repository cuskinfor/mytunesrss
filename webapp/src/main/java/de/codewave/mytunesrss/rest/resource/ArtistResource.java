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
import de.codewave.mytunesrss.rest.representation.ArtistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultSetType;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;

/**
 * Artist operations.
 */
@ValidateRequest
@Path("artist/{artist}")
@RequiredUserPermissions({UserPermission.Audio})
public class ArtistResource extends RestResource {

    /**
     * Get the representation of an artist.
     *
     * @param artist The artist name.
     *
     * @return The artist representation.
     *
     * @throws SQLException
     */
    @GET
    @Produces({"application/json"})
    @GZIP
    public ArtistRepresentation getArtist(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("artist") String artist
    ) throws SQLException {
        FindArtistQuery query = new FindArtistQuery(MyTunesRssWebUtils.getAuthUser(request), artist, null, null, -1);
        query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(query);
        for (Artist result = queryResult.nextResult(); result != null; result = queryResult.nextResult()) {
            if (result.getName().equals(artist)) {
                return toArtistRepresentation(uriInfo, request, result);
            }
        }
        return null;
    }

    /**
     * Get the albums of an artist.
     *
     * @param filter Filter for album name (ony matching ones are returned).
     * @param artist Artist name.
     * @param genres Filter for album genres, can be specified multiple times (ony matching albums are returned).
     * @param minYear Filter for minimum album year (ony matching ones are returned).
     * @param maxYear Filter for maximum album year (ony matching ones are returned).
     * @param sortYear "true" to sort results by year or "false" to return in database order.
     * @param groupByType "true" to return normal albums before compilations or "false" to mix both types.
     * @param type Filter for album type (One of "COMPILATIONS", "ALBUMS", "ALL").
     *
     * @return List of albums.
     *
     * @throws SQLException
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.AlbumRepresentation>
     */
    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public Iterable<AlbumRepresentation> getAlbums(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("filter") String filter,
            @PathParam("artist") String artist,
            @QueryParam("genre") String[] genres,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("groupByType") @DefaultValue("false") boolean groupByType,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        FindAlbumQuery findAlbumQuery = new FindAlbumQuery(MyTunesRssWebUtils.getAuthUser(request), filter, artist, false, genres, -1, minYear, maxYear, sortYear, groupByType, type);
        findAlbumQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(findAlbumQuery);
        return toAlbumRepresentations(uriInfo, request, queryResult);
    }

    /**
     * Get the tracks of an artist.
     *
     * @param artist Artist name.
     * @param sortOrder Sort order of the results (One of "Album", "Artist", "KeepOrder").
     *
     * @return List of tracks.
     *
     * @throws SQLException
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.TrackRepresentation>
     */
    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public Iterable<TrackRepresentation> getTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("artist") String artist,
            @QueryParam("sort") @DefaultValue("Album") SortOrder sortOrder
    ) throws SQLException {
        FindTrackQuery findTrackQuery = FindTrackQuery.getForArtist(MyTunesRssWebUtils.getAuthUser(request), new String[]{artist}, sortOrder);
        findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        return toTrackRepresentations(uriInfo, request, queryResult);
    }

}
