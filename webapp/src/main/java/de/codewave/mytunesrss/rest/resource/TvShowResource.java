/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.ws.rs.*;
import java.sql.SQLException;
import java.util.*;

@ValidateRequest
@Path("tvshow/{show}")
public class TvShowResource extends RestResource {

    @GET
    @Path("seasons")
    @Produces({"application/json"})
    @GZIP
    public Map<Integer, List<TrackRepresentation>> getSeasons(
            @PathParam("show") String show
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowSeriesEpisodes(getAuthUser(), show));
        Map<Integer, List<TrackRepresentation>> result = new LinkedHashMap<Integer, List<TrackRepresentation>>();
        for (Track track : queryResult.getResults()) {
            if (!result.containsKey(track.getSeason())) {
                result.put(track.getSeason(), new ArrayList<TrackRepresentation>());
            }
            result.get(track.getSeason()).add(toTrackRepresentation(track));
        }
        return result;
    }

    @GET
    @Path("season/{season}/episodes")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getEpisodes(
            @PathParam("show") String show,
            @PathParam("season") int season
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowSeriesSeasonEpisodes(getAuthUser(), show, season));
        return toTrackRepresentations(queryResult.getResults());
    }

}
