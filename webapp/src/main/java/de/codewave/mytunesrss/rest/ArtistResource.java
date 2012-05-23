/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ValidateRequest
@Path("artist/{artist}")
public class ArtistResource extends RestResource {

    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public List<Album> getAlbums(
            @QueryParam("filter") String filter,
            @PathParam("artist") String artist,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAlbumQuery(getAuthUser(), filter, artist, genre, index, minYear, maxYear, sortYear, type));
        return queryResult.getResults();
    }

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<Track> getTracks(
            @PathParam("artist") String artist,
            @QueryParam("sort") @DefaultValue("Album") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForArtist(getAuthUser(), new String[] {artist}, sortOrder));
        return queryResult.getResults();
    }

    @GET
    @Path("album/{album}/tracks")
    @Produces({"application/json"})
    @GZIP
    public List<Track> getArtistAlbumTracks(
            @PathParam("artist") String artist,
            @PathParam("album") String album,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(getAuthUser(), new String[]{album}, new String[]{artist}, sortOrder));
        return queryResult.getResults();
    }

    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("artist") String artist
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForArtistQuery(artist));
        return queryResult.getResults();
    }

    @POST
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void getTags(
            @PathParam("artist") String artist,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTracks(artist), tag));
        }
    }

    private String[] getTracks(String artist) throws SQLException {
        List<Track> tracks = getTracks(artist, SortOrder.KeepOrder);
        Set<String> trackIds = new HashSet<String>();
        for (Track track : tracks) {
            trackIds.add(track.getId());
        }
        return trackIds.toArray(new String[trackIds.size()]);
    }

    @DELETE
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void deleteTags(
            @PathParam("artist") String artist,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTracks(artist), tag));
        }
    }
}
