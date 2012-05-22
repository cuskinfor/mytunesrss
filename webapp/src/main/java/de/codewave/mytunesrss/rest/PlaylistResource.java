/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.List;

@ValidateRequest
@Path("playlist/{id}")
public class PlaylistResource extends RestResource {

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    public List<Track> getPlaylistTracks(
            @PathParam("id") @NotBlank(message = "Playlist id must not be blank.") String id,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(), id, sortOrder));
        return queryResult.getResults();
    }

    @GET
    @Path("playlists")
    @Produces({"application/json"})
    public List<Playlist> getPlaylistChildren(
            @PathParam("id") @NotBlank(message = "Playlist id must not be blank.") String id,
            @QueryParam("hidden") @DefaultValue("false") boolean includeHidden,
            @QueryParam("owner") @DefaultValue("false") boolean matchingOwner,
            @QueryParam("type") List<PlaylistType> types
    ) throws SQLException {
        DataStoreQuery.QueryResult<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(), types, null, id, includeHidden, matchingOwner));
        return queryResult.getResults();
    }

}
