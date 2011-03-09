/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTvShowCommandHandler extends BrowseVideoCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseTvShowCommandHandler.class);

    @Override
    protected List<? extends Track> getEnhancedTracks(List<Track> tracks) {
        return TrackUtils.getTvShowEpisodes(getTransaction(), tracks);
    }

    @Override
    protected DataStoreQuery<DataStoreQuery.QueryResult<Track>> getQuery() {
        String series = getRequestParameter("series", null);
        int season = getIntegerRequestParameter("season", -1);
        if (series != null && season > -1) {
            return FindTrackQuery.getTvShowSeriesSeasonEpisodes(getAuthUser(), series, season);
        } else if (series != null) {
            return FindTrackQuery.getTvShowSeriesEpisodes(getAuthUser(), series);
        } else {
            return FindTrackQuery.getTvShowEpisodes(getAuthUser());
        }
    }

    @Override
    protected MyTunesRssResource getResource(List<? extends Track> tracks) {
        List<TrackUtils.TvShowEpisode> episodes = (List<TrackUtils.TvShowEpisode>) tracks;
        int seriesCount = 0;
        int seasonCount = 0;
        for (TrackUtils.TvShowEpisode episode : episodes) {
            if (episode.isNewSeries()) {
                seriesCount++;
            }
            if (episode.isNewSeason()) {
                seasonCount++;
            }
        }
        LOGGER.debug("Browsing TV shows: {} show(s), {} season(s), {} episode(s).", new Object[] {seriesCount, seasonCount, tracks.size()});
        if (seriesCount == 1 && seasonCount == 1) {
            return MyTunesRssResource.BrowseTvShow; // TODO: single season of a show
        } else if (seriesCount == 1) {
            return MyTunesRssResource.BrowseTvShow; // TODO: multiple seasons of a show
        } else {
            return MyTunesRssResource.BrowseTvShow; // TODO: multiple shows
        }
    }
}