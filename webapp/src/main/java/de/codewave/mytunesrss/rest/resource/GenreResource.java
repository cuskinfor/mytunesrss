/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.List;

@ValidateRequest
@Path("genre/{genre}")
public class GenreResource extends RestResource {

    @GET
    @Path("tracks")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getGenreTracks(
            @PathParam("genre") String genre,
            @QueryParam("sort") @DefaultValue("KeepOrder") SortOrder sortOrder
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForGenre(getAuthUser(), new String[] {MyTunesRssBase64Utils.decodeToString(genre)}, sortOrder));
        return toTrackRepresentations(queryResult.getResults());
    }

}
