/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.rest.RequiredUserPermissions;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.TrackRepresentation;
import de.codewave.mytunesrss.rest.representation.TvShowSeasonRepresentation;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
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

/**
 * TV show operations.
 */
@ValidateRequest
@Path("tvshow/{show}")
@RequiredUserPermissions({UserPermission.Video})
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
        QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getTvShowSeriesEpisodes(MyTunesRssWebUtils.getAuthUser(request), show));
        Map<Integer, MutableInt> episodesPerSeason = new HashMap<>();
        Map<Integer, String> imageHashPerEpisode = new HashMap<>();
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
        List<TvShowSeasonRepresentation> seasons = new ArrayList<>();
        for (Map.Entry<Integer, MutableInt> entry : episodesPerSeason.entrySet()) {
            TvShowSeasonRepresentation representation = new TvShowSeasonRepresentation();
            representation.setName(entry.getKey());
            representation.setEpisodeCount(entry.getValue().intValue());
            representation.setEpisodesUri(uriInfo.getBaseUriBuilder().path(TvShowResource.class).path(TvShowResource.class, "getEpisodes").build(show, entry.getKey()).toString());
            if (imageHashPerEpisode.containsKey(entry.getKey())) {
                representation.setImageHash(StringUtils.trimToNull(imageHashPerEpisode.get(entry.getKey())));
                representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc("hash=" + imageHashPerEpisode.get(entry.getKey()))).toString());
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
     *
     * @responseType java.util.List<de.codewave.mytunesrss.rest.representation.TrackRepresentation>
     */
    @GET
    @Path("season/{season}/episodes")
    @Produces({"application/json"})
    @GZIP
    public Iterable<TrackRepresentation> getEpisodes(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @PathParam("show") String show,
            @PathParam("season") int season
    ) throws SQLException {
        FindTrackQuery findTrackQuery = FindTrackQuery.getTvShowSeriesSeasonEpisodes(MyTunesRssWebUtils.getAuthUser(request), show, season);
        findTrackQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        QueryResult<Track> queryResult = TransactionFilter.getTransaction().executeQuery(findTrackQuery);
        return toTrackRepresentations(uriInfo, request, queryResult);
    }

}
