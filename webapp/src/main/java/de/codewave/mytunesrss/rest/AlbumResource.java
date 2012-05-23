/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public List<Track> getAlbumTracks(
            @PathParam("album") String album,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(getAuthUser(), new String[]{album}, null, sortOrder));
        return queryResult.getResults();
    }

    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("album") String album
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForAlbumQuery(album));
        return queryResult.getResults();
    }

    @POST
    @Path("tags")
    @Consumes("application/x-www-form-urlencoded")
    public void getTags(
            @PathParam("album") String album,
            @FormParam("tag") List<String> tags
    ) throws SQLException {
        for (String tag : tags) {
            TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(getTracks(album), tag));
        }
    }

    private String[] getTracks(String album) throws SQLException {
        List<Track> tracks = getAlbumTracks(album, SortOrder.KeepOrder);
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
            TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(getTracks(album), tag));
        }
    }
}
