/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public abstract class BrowseVideoCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForMediaAndVideoTypes(getAuthUser(), new MediaType[] {MediaType.Video}, getVideoType());
            List<TrackUtils.EnhancedTrack> tracks = null;
            if (query != null) {
                DataStoreQuery.QueryResult<Track> result = getTransaction().executeQuery(query);
                int pageSize = getWebConfig().getEffectivePageSize();
                TrackUtils.EnhancedTracks enhancedTracks;
                if (pageSize > 0 && result.getResultSize() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(result.getResultSize(), current);
                    getRequest().setAttribute("pager", pager);
                    enhancedTracks = TrackUtils.getEnhancedTracks(getTransaction(), result.getResults(current * pageSize, pageSize), SortOrder.KeepOrder);
                } else {
                    enhancedTracks = TrackUtils.getEnhancedTracks(getTransaction(), result.getResults(), SortOrder.KeepOrder);
                }
                tracks = (List<TrackUtils.EnhancedTrack>)enhancedTracks.getTracks();
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
                forward(getResource());
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    protected abstract MyTunesRssResource getResource();

    protected abstract VideoType getVideoType();
}