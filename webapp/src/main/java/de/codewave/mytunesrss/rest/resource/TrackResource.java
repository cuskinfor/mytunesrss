/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.datastore.statement.FindAllTagsForTrackQuery;
import de.codewave.mytunesrss.datastore.statement.RemoveTagFromTracksStatement;
import de.codewave.mytunesrss.datastore.statement.SetTagToTracksStatement;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.List;

@ValidateRequest
@Path("track/{track}")
public class TrackResource extends RestResource {

    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("track") String track
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForTrackQuery(track));
        return queryResult.getResults();
    }

    @PUT
    @Path("tag/{tag}")
    @Produces({"application/json"})
    public List<String> setTag(
            @PathParam("track") String track,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(new String[] {track}, tag));
        return getTags(track);
    }

    @DELETE
    @Path("tag/{tag}")
    @Produces({"application/json"})
    public List<String> deleteTag(
            @PathParam("track") String track,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(new String[] {track}, tag));
        return getTags(track);
    }
}
