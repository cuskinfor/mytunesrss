/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;

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
            DataStoreQuery<QueryResult<Track>> query = getQuery();
            List<? extends Track> tracks = null;
            if (query != null) {
                tracks = getEnhancedTracks(getTransaction().executeQuery(query).getResults());
                int pageSize = getWebConfig().getEffectivePageSize();
                if (pageSize > 0 && tracks.size() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(tracks.size(), current);
                    getRequest().setAttribute("pager", pager);
                    tracks = MyTunesRssUtils.getSubList(tracks, current * pageSize, pageSize);
                }
                getRequest().setAttribute("tracks", tracks);
            }
            if (tracks == null || tracks.isEmpty()) {
                addError(new BundleError("error.browseTrackNoResult"));
                if (StringUtils.isNotEmpty(getRequestParameter("backUrl", null))) {
                    redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
                } else {
                    getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
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

    protected abstract List<? extends Track> getEnhancedTracks(List<Track> tracks);

    protected abstract DataStoreQuery<QueryResult<Track>> getQuery();

    protected abstract MyTunesRssResource getResource();
}
