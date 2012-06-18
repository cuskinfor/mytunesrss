/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.representation.AlbumRepresentation;
import de.codewave.mytunesrss.rest.representation.ArtistRepresentation;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
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
            @PathParam("artist") String artist
    ) throws SQLException {
        DataStoreQuery.QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindArtistQuery(getAuthUser(), artist, null, null, -1));
        for (Artist result = queryResult.nextResult(); result != null; result = queryResult.nextResult()) {
            if (result.getName().equals(artist)) {
                return toArtistRepresentation(result);
            }
        }
        return null;
    }

    /**
     * Get the albums of an artist.
     *
     * @param filter Filter for album name (ony matching ones are returned).
     * @param artist Artist name.
     * @param genre Filter for album genre (ony matching ones are returned).
     * @param minYear Filter for minimum album year (ony matching ones are returned).
     * @param maxYear Filter for maximum album year (ony matching ones are returned).
     * @param sortYear "true" to sort results by year or "false" to return in database order.
     * @param type Filter for album type (One of "COMPILATIONS", "ALBUMS", "ALL").
     *
     * @return List of albums.
     *
     * @throws SQLException
     */
    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public List<AlbumRepresentation> getAlbums(
            @QueryParam("filter") String filter,
            @PathParam("artist") String artist,
            @QueryParam("genre") String genre,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAlbumQuery(getAuthUser(), filter, artist, genre, -1, minYear, maxYear, sortYear, type));
        return toAlbumRepresentations(queryResult.getResults());
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
     */
    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getTracks(
            @PathParam("artist") String artist,
            @QueryParam("sort") @DefaultValue("Album") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForArtist(getAuthUser(), new String[] {artist}, sortOrder));
        return toTrackRepresentations(queryResult.getResults());
    }

    /**
     * Get a list of tags for the artist, i.e. a list of tags for all tracks. All tags of all tracks of the artist are returned,
     * i.e. not all tracks have all the tags returned but only at least one track has each of the tags.
     *
     * @param artist The artist name.
     *
     * @return List of all tags.
     *
     * @throws SQLException
     */
    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("artist") String artist
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForArtistQuery(artist));
        return queryResult.getResults();
    }

    /**
     * Set a tag to all tracks of the artist.
     *
     * @param artist The artist name.
     * @param tag The tag to set.
     *
     * @return List of all tags after adding the specified one.
     *
     * @throws SQLException
     */
    @PUT
    @Path("tag/{tag}")
    @Produces({"application/json"})
    public List<String> setTag(
            @PathParam("artist") String artist,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTrackIds(artist), tag));
        return getTags(artist);
    }

    private String[] getTrackIds(String artist) throws SQLException {
        List<TrackRepresentation> tracks = getTracks(artist, SortOrder.KeepOrder);
        Set<String> trackIds = new HashSet<String>();
        for (TrackRepresentation track : tracks) {
            trackIds.add(track.getId());
        }
        return trackIds.toArray(new String[trackIds.size()]);
    }

    /**
     * Delete a tag from all tracks of an album.
     *
     * @param artist The artist name.
     * @param tag The tag to delete.
     *
     * @return List of all tags after deleting the specified one.
     *
     * @throws SQLException
     */
    @DELETE
    @Path("tag/{tag}")
    @Produces({"application/json"})
    public List<String> deleteTag(
            @PathParam("artist") String artist,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTrackIds(artist), tag));
        return getTags(artist);
    }
}
