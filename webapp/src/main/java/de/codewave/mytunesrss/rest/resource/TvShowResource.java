/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.rest.representation.TvShowRepresentation;
import de.codewave.mytunesrss.rest.representation.TvShowSeasonRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
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
import java.util.*;

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
    public List<TvShowSeasonRepresentation> getSeasons(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("show") String show
    ) throws SQLException {
        DataStoreQuery.QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowSeriesEpisodes(MyTunesRssWebUtils.getAuthUser(request), show));
        Map<Integer, MutableInt> episodesPerSeason = new HashMap<Integer, MutableInt>();
        Map<Integer, String> imageHashPerEpisode = new HashMap<Integer, String>();
        for (Track track = queryResult.nextResult(); track != null; track = queryResult.nextResult()) {
            if (!imageHashPerEpisode.containsKey(track.getSeason())) {
                imageHashPerEpisode.put(track.getSeason(), track.getImageHash());
            }
            if (episodesPerSeason.containsKey(track.getSeason())) {
                episodesPerSeason.get(track.getSeason()).increment();
            } else {
                episodesPerSeason.put(track.getSeason(), new MutableInt(1));
            }
        }
        List<TvShowSeasonRepresentation> seasons = new ArrayList<TvShowSeasonRepresentation>();
        for (Map.Entry<Integer, MutableInt> entry : episodesPerSeason.entrySet()) {
            TvShowSeasonRepresentation representation = new TvShowSeasonRepresentation();
            representation.setName(entry.getKey());
            representation.setEpisodeCount(entry.getValue().intValue());
            representation.setEpisodesUri(uriInfo.getBaseUriBuilder().path(TvShowResource.class).path(TvShowResource.class, "getEpisodes").build(show, entry.getKey()));
            if (imageHashPerEpisode.containsKey(entry.getKey())) {
                representation.setImageHash(StringUtils.trimToNull(imageHashPerEpisode.get(entry.getKey())));
                representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, "hash=" + imageHashPerEpisode.get(entry.getKey())));
            }
            seasons.add(representation);
        }
        Collections.sort(seasons);
        return seasons;
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
