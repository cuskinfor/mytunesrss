/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ValidateRequest
@Path("tvshow/{show}")
public class TvShowResource extends RestResource {

    /**
     * Get a list of all seasons for a TV show.
     *
     * @param show A TV show name.
     *
     * @return List of all seasons.
     *
     * @throws SQLException
     */
    @GET
    @Path("seasons")
    @Produces({"application/json"})
    @GZIP
    public Map<Integer, List<TrackRepresentation>> getSeasons(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("show") String show
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowSeriesEpisodes(MyTunesRssWebUtils.getAuthUser(request), show));
        Map<Integer, List<TrackRepresentation>> result = new LinkedHashMap<Integer, List<TrackRepresentation>>();
        for (Track track : queryResult.getResults()) {
            if (!result.containsKey(track.getSeason())) {
                result.put(track.getSeason(), new ArrayList<TrackRepresentation>());
            }
            result.get(track.getSeason()).add(toTrackRepresentation(uriInfo, request, track));
        }
        return result;
    }

    /**
     * Get a list of all episodes of a TV show season.
     *
     * @param show A TV show name.
     * @param season A season number.
     *
     * @return List of all episodes.
     *
     * @throws SQLException
     */
    @GET
    @Path("season/{season}/episodes")
    @Produces({"application/json"})
    @GZIP
    public List<TrackRepresentation> getEpisodes(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("show") String show,
            @PathParam("season") int season
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowSeriesSeasonEpisodes(MyTunesRssWebUtils.getAuthUser(request), show, season));
        return toTrackRepresentations(uriInfo, request, queryResult.getResults());
    }

}
