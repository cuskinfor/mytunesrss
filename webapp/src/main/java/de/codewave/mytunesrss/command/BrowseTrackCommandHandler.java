/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.lucene.LuceneQueryParserException;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTrackCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (getRequest().getPathInfo() != null && getRequest().getPathInfo().toLowerCase().endsWith(".gif")) {
            return;// fix for those stupid yahoo media player img requests
        }
        if (isSessionAuthorized()) {
            String searchTerm = getRequestParameter("searchTerm", null);
            String sortOrderName = getRequestParameter("sortOrder", SortOrder.Album.name());
            SortOrder sortOrderValue = SortOrder.valueOf(sortOrderName);

            OffHeapSessionStore offHeapSessionStore = OffHeapSessionStore.get(getRequest());
            String currentListId = getRequestParameter(OffHeapSessionStore.CURRENT_LIST_ID, null);
            List<Track> cachedTracks = offHeapSessionStore.getCurrentList(currentListId);

            DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = null;

            if (StringUtils.isNotEmpty(searchTerm) && (cachedTracks == null || sortOrderValue != offHeapSessionStore.getCurrentSortOrder())) {
                StopWatch.start("Lucene search and query preparation");
                if (getWebConfig().getSearchFuzziness() == -1) {
                    try {
                        query = FindTrackQuery.getForExpertSearchTerm(getAuthUser(), searchTerm, sortOrderValue, getWebConfig().getMaxSearchResults());
                    } catch (LuceneQueryParserException e) {
                        addError(new BundleError("error.illegalExpertSearchTerm"));
                        forward(MyTunesRssCommand.ShowPortal);
                        return; // early return
                    }
                } else {
                    int maxTermSize = 0;
                    for (String term : searchTerm.split(" ")) {
                        if (term.length() > maxTermSize) {
                            maxTermSize = term.length();
                        }
                    }
                    if (maxTermSize >= 2) {
                        query = FindTrackQuery.getForSearchTerm(getAuthUser(), searchTerm, getWebConfig().getSearchFuzziness(), sortOrderValue, getWebConfig().getMaxSearchResults());
                    } else {
                        addError(new BundleError("error.searchTermMinSize", 2));
                        forward(MyTunesRssCommand.ShowPortal);
                        return; // early return
                    }
                }
                StopWatch.stop();
            } else {
                query = TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
                if (query instanceof FindPlaylistTracksQuery) {// keep sort order for playlists
                    sortOrderValue = SortOrder.KeepOrder;
                }
                if (cachedTracks != null && sortOrderValue == offHeapSessionStore.getCurrentSortOrder()) {
                    query = null;
                }
            }

            if (query != null) {
                query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
                currentListId = offHeapSessionStore.newCurrentList();
                cachedTracks = offHeapSessionStore.getCurrentList(currentListId);
                StopWatch.start("SQL query for tracks");
                getTransaction().executeQuery(query).addRemainingResults(cachedTracks);
                StopWatch.stop();
                offHeapSessionStore.setCurrentSortOrder(sortOrderValue);
            }

            List<TrackUtils.EnhancedTrack> tracks = null;

            if (cachedTracks != null) {
                getRequest().setAttribute(OffHeapSessionStore.CURRENT_LIST_ID, currentListId);

                int pageSize = getWebConfig().getEffectivePageSize();
                TrackUtils.EnhancedTracks enhancedTracks;
                StopWatch.start("Creating enhanced tracks");
                if (pageSize > 0 && cachedTracks.size() > pageSize) {
                    int current = getSafeIntegerRequestParameter("index", 0);
                    Pager pager = createPager(cachedTracks.size(), current);
                    getRequest().setAttribute("pager", pager);
                    enhancedTracks = TrackUtils.getEnhancedTracks(getTransaction(), MyTunesRssUtils.getSubList(cachedTracks, current * pageSize, pageSize), sortOrderValue);
                } else {
                    enhancedTracks = TrackUtils.getEnhancedTracks(getTransaction(), cachedTracks, sortOrderValue);
                }
                StopWatch.stop();
                getRequest().setAttribute("sortOrderLink", Boolean.valueOf(!enhancedTracks.isSimpleResult()) && sortOrderValue != SortOrder.KeepOrder);
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
                    getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
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
                getRequest().setAttribute("sortOrder", sortOrderValue.name());
                forward(MyTunesRssResource.BrowseTrack);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
