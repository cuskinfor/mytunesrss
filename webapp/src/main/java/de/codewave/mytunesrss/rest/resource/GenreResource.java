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
 * Genre operations.
 */
@ValidateRequest
@Path("genre/{genre}")
@RequiredUserPermissions({UserPermission.Audio})
public class GenreResource extends RestResource {

    /**
     * Get the tracks of the genre.
     *
     * @param genre The genre.
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
    public Iterable<TrackRepresentation> getGenreTracks(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("genre") String genre,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        FindTrackQuery findTrackQuery = FindTrackQuery.getForGenre(MyTunesRssWebUtils.getAuthUser(request), new String[]{genre}, sortOrder);
        findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        return toTrackRepresentations(uriInfo, request, queryResult);
    }

    /**
     * Get albums with tracks of the genre.
     *
     * @param genre The genre.
     * @param index The index can be "-1" for all, "0" for albums in the section "0-9", "1" for the section "A-C",
     *              "2" for "D-F", "3" for "G-I", "4" for "J-L", "5" for "M-O", "6" for "P-S", "7" for "T-V" and
     *              "8" for "W-Z".
     * @param minYear Minimum album year filter.
     * @param maxYear Maximum album year filter.
     * @param sortYear "true" to sort albums by year or "false" to sort by name.
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
    public Iterable<AlbumRepresentation> getGenreAlbums(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("groupByType") @DefaultValue("false") boolean groupByType,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        FindAlbumQuery findAlbumQuery = new FindAlbumQuery(MyTunesRssWebUtils.getAuthUser(request), null, null, false, new String[]{genre}, index, minYear, maxYear, sortYear, groupByType, type);
        findAlbumQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(findAlbumQuery);
        return toAlbumRepresentations(uriInfo, request, queryResult);
    }

    /**
     * Get artists with tracks of the genre.
     *
     * @param genre The genre.
     * @param index The index can be "-1" for all, "0" for albums in the section "0-9", "1" for the section "A-C",
     *              "2" for "D-F", "3" for "G-I", "4" for "J-L", "5" for "M-O", "6" for "P-S", "7" for "T-V" and
     *              "8" for "W-Z".
     *
     * @return A list of artists.
     *
     * @throws SQLException
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.ArtistRepresentation>
     */
    @GET
    @Path("artists")
    @Produces({"application/json"})
    @GZIP
    public Iterable<ArtistRepresentation> getGenreArtists(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index
    ) throws SQLException {
        FindArtistQuery findArtistQuery = new FindArtistQuery(MyTunesRssWebUtils.getAuthUser(request), null, null, new String[]{genre}, index);
        findArtistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(findArtistQuery);
        return toArtistRepresentations(uriInfo, request, queryResult);
    }
}
