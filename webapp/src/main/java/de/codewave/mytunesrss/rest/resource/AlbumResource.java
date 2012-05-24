/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ValidateRequest
@Path("album/{album}")
public class AlbumResource extends RestResource {

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getAlbumTracks(
            @PathParam("album") String album,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(getAuthUser(), new String[]{MyTunesRssBase64Utils.decodeToString(album)}, null, sortOrder));
        return toTrackRepresentations(queryResult.getResults());
    }

    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("album") String album
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForAlbumQuery(MyTunesRssBase64Utils.decodeToString(album)));
        return queryResult.getResults();
    }

    @POST
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void setTags(
            @PathParam("album") String album,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTracks(MyTunesRssBase64Utils.decodeToString(album)), tag));
        }
    }

    private String[] getTracks(String album) throws SQLException {
        List<? extends Track> tracks = getAlbumTracks(album, SortOrder.KeepOrder);
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
            @PathParam("album") String album,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTracks(MyTunesRssBase64Utils.decodeToString(album)), tag));
        }
    }
}
