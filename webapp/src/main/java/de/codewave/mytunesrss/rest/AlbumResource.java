/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.List;

@ValidateRequest
@Path("artist/{artist}/album/{album}")
public class AlbumResource extends RestResource {

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    public List<Track> getAlbumTracks(
            @PathParam("artist") String artist,
            @PathParam("album") String album,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForAlbum(getAuthUser(), new String[]{album}, new String[]{artist}, sortOrder));
        return queryResult.getResults();
    }

}
