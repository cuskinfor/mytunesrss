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

    /**
     * Get a list of tags for the track.
     *
     * @param track A track ID.
     *
     * @return List of all tags.
     *
     * @throws SQLException
     */
    @GET
    @Path("tags")
    @Produces({"application/json"})
    public List<String> getTags(
            @PathParam("track") String track
    ) throws SQLException {
        DataStoreQuery.QueryResult<String> queryResult = TransactionFilter.getTransaction().executeQuery(new FindAllTagsForTrackQuery(track));
        return queryResult.getResults();
    }

    /**
     * Set a tag to the track.
     *
     * @param track A track ID.
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
            @PathParam("track") String track,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new SetTagToTracksStatement(new String[] {track}, tag));
        return getTags(track);
    }

    /**
     * Delete a tag from a track.
     *
     * @param track A track ID.
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
            @PathParam("track") String track,
            @PathParam("tag") String tag
    ) throws SQLException {
        TransactionFilter.getTransaction().executeStatement(new RemoveTagFromTracksStatement(new String[] {track}, tag));
        return getTags(track);
    }
}
