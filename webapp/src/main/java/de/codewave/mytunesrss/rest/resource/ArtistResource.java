/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
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

    @GET
    @Produces({"application/json"})
    @GZIP
    public ArtistRepresentation getArtist(
            @PathParam("artist") String artist
    ) throws SQLException {
        String decodedArtist = MyTunesRssBase64Utils.decodeToString(artist);
        DataStoreQuery.QueryResult<Artist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindArtistQuery(getAuthUser(), decodedArtist, null, null, -1));
        for (Artist result = queryResult.nextResult(); result != null; result = queryResult.nextResult()) {
            if (result.getName().equals(decodedArtist)) {
                return toArtistRepresentation(result);
            }
        }
        return null;
    }

    @GET
    @Path("albums")
    @Produces({"application/json"})
    @GZIP
    public List<AlbumRepresentation> getAlbums(
            @QueryParam("filter") String filter,
            @PathParam("artist") String artist,
            @QueryParam("genre") String genre,
            @QueryParam("index") @DefaultValue("-1") @Range(min = -1, max = 8, message = "Index must be a value from -1 to 8.") int index,
            @QueryParam("minYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Minimum year must be a value from -1 to 9999.") int minYear,
            @QueryParam("maxYear") @DefaultValue("-1") @Range(min = -1, max = 9999, message = "Maximum year must be a value from -1 to 9999.") int maxYear,
            @QueryParam("sortYear") @DefaultValue("false") boolean sortYear,
            @QueryParam("type") @DefaultValue("ALL")FindAlbumQuery.AlbumType type
    ) throws SQLException {
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAlbumQuery(getAuthUser(), filter, MyTunesRssBase64Utils.decodeToString(artist), genre, index, minYear, maxYear, sortYear, type));
        return toAlbumRepresentations(queryResult.getResults());
    }

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getTracks(
            @PathParam("artist") String artist,
            @QueryParam("sort") @DefaultValue("Album") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForArtist(getAuthUser(), new String[] {MyTunesRssBase64Utils.decodeToString(artist)}, sortOrder));
        return toTrackRepresentations(queryResult.getResults());
    }

    @GET
    @Path("album/{album}")
    @Produces({"application/json"})
    @GZIP
    public AlbumRepresentation getArtistAlbum(
            @PathParam("artist") String artist,
            @PathParam("album") String album
    ) throws SQLException {
        String decodedAlbum = MyTunesRssBase64Utils.decodeToString(album);
        DataStoreQuery.QueryResult<Album> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAlbumQuery(getAuthUser(), decodedAlbum, MyTunesRssBase64Utils.decodeToString(artist), null, 0, -1, -1, true, FindAlbumQuery.AlbumType.ALL));
        for (Album result = queryResult.nextResult(); result != null; result = queryResult.nextResult()) {
            if (result.getName().equals(decodedAlbum)) {
                return toAlbumRepresentation(result);
            }
        }
        return null;
    }

    @GET
    @Path("album/{album}/tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getArtistAlbumTracks(
            @PathParam("artist") String artist,
            @PathParam("album") String album,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(getAuthUser(), new String[]{MyTunesRssBase64Utils.decodeToString(album)}, new String[]{MyTunesRssBase64Utils.decodeToString(artist)}, sortOrder));
        return toTrackRepresentations(queryResult.getResults());
    }

    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("artist") String artist
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForArtistQuery(MyTunesRssBase64Utils.decodeToString(artist)));
        return queryResult.getResults();
    }

    @POST
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void setTags(
            @PathParam("artist") String artist,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTracks(MyTunesRssBase64Utils.decodeToString(artist)), tag));
        }
    }

    private String[] getTracks(String artist) throws SQLException {
        List<? extends Track> tracks = getTracks(artist, SortOrder.KeepOrder);
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
            TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTracks(MyTunesRssBase64Utils.decodeToString(artist)), tag));
        }
    }
}
