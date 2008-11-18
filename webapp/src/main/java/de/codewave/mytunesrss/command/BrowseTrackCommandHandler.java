/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.TrackUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTrackCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(BrowseTrackCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (getRequest().getPathInfo() != null && getRequest().getPathInfo().toLowerCase().endsWith(".gif")) {
            return;// fix for those stupid yahoo media player img requests
        }
        if (isSessionAuthorized()) {
            String searchTerm = getRequestParameter("searchTerm", null);
            String sortOrderName = getRequestParameter("sortOrder", FindPlaylistTracksQuery.SortOrder.Album.name());
            FindPlaylistTracksQuery.SortOrder sortOrderValue = FindPlaylistTracksQuery.SortOrder.valueOf(sortOrderName);

            DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = null;
            if (StringUtils.isNotEmpty(searchTerm)) {
                int maxTermSize = 0;
                for (String term : searchTerm.split(" ")) {
                    if (term.length() > maxTermSize) {
                        maxTermSize = term.length();
                    }
                }
                if (maxTermSize >= 3) {
                    query = FindTrackQuery.getForSearchTerm(getAuthUser(), searchTerm, sortOrderValue == FindPlaylistTracksQuery.SortOrder.Artist);
                } else {
                    addError(new BundleError("error.searchTermMinSize", 3));
                    forward(MyTunesRssCommand.ShowPortal);
                    return;// early return
                }
            } else {
                query = TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
            }
            getRequest().setAttribute("sortOrder", sortOrderName);
            List<TrackUtils.EnhancedTrack> tracks = null;
            if (query != null) {
                DataStoreQuery.QueryResult<Track> result = getTransaction().executeQuery(query);
                int pageSize = getWebConfig().getEffectivePageSize();
                TrackUtils.EnhancedTracks enhancedTracks;
                if (query instanceof FindPlaylistTracksQuery) {// keep sort order for playlists
                    sortOrderValue = FindPlaylistTracksQuery.SortOrder.KeepOrder;
                }
                if (pageSize > 0 && result.getResultSize() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(result.getResultSize(), current);
                    getRequest().setAttribute("pager", pager);
                    enhancedTracks = TrackUtils.getEnhancedTracks(getTransaction(), result.getResults(current * pageSize, pageSize), sortOrderValue);
                } else {
                    enhancedTracks = TrackUtils.getEnhancedTracks(getTransaction(), result.getResults(), sortOrderValue);
                }
                getRequest().setAttribute("sortOrderLink", Boolean.valueOf(!enhancedTracks.isSimpleResult()) && sortOrderValue != FindPlaylistTracksQuery.SortOrder.KeepOrder);
                tracks = (List<TrackUtils.EnhancedTrack>)enhancedTracks.getTracks();
                if (pageSize > 0 && tracks.size() > pageSize) {
                    tracks.get(0).setContinuation(!tracks.get(0).isNewSection());
                    tracks.get(0).setNewSection(true);
                }
                getRequest().setAttribute("tracks", tracks);
            }
            if (tracks == null || tracks.isEmpty()) {
                addError(new BundleError("error.browseTrackNoResult"));
                if (StringUtils.isNotEmpty(getRequestParameter("backUrl", null))) {
                    redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
                } else {
                    getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
                }
            } else {
                DataStoreQuery.QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                                Collections.singletonList(
                                                                                                                                        PlaylistType.MyTunes),
                                                                                                                                null,
                                                                                                                                null,
                                                                                                                                false,
                                                                                                                                true));
                getRequest().setAttribute("editablePlaylists", playlistsQueryResult.getResults());
                forward(MyTunesRssResource.BrowseTrack);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}