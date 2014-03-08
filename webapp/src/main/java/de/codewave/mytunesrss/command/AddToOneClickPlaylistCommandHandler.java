/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.CreateOneClickPlaylistCommandHandler
 */
public class AddToOneClickPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            String playlistId = getRequestParameter("playlistId", null);
            SavePlaylistStatement statement;
            List<String> trackIds = new ArrayList<>();
            if (StringUtils.isNotBlank(playlistId)) {
                FindPlaylistQuery query = new FindPlaylistQuery(getAuthUser(), Collections.singletonList(PlaylistType.MyTunes), playlistId, null, true, true);
                List<Playlist> queryResult = getTransaction().executeQuery(query).getResults();
                if (queryResult == null || queryResult.size() != 1) {
                    throw new IllegalArgumentException("Illegal playlist ID.");
                }
                Playlist playlist = queryResult.get(0);
                for (Track track : TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(getAuthUser(), playlist.getId(), null)).getResults()) {
                    trackIds.add(track.getId());
                }
                statement = new SaveMyTunesPlaylistStatement(playlist.getUserOwner(), playlist.isUserPrivate());
                statement.setName(playlist.getName());
                statement.setId(playlist.getId());
                statement.setContainerId(playlist.getContainerId());
            } else {
                statement = new SaveMyTunesPlaylistStatement(getAuthUser().getName(), false);
                statement.setName(getRequestParameter("playlistName", "new playlist"));
            }
            trackIds.addAll(getTrackIds(getTransaction().executeQuery(getQuery()).getResults()));
            statement.setTrackIds(trackIds);
            getTransaction().executeStatement(statement);
            String backUrl = MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null));
            if (StringUtils.isNotEmpty(backUrl)) {
                redirect(backUrl);
            } else {
                forward(MyTunesRssCommand.ShowPortal);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

    private DataStoreQuery<QueryResult<Track>> getQuery() throws SQLException {
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        DataStoreQuery<QueryResult<Track>> query = null;
        if (trackIds != null && trackIds.length > 0) {
            return FindTrackQuery.getForIds(trackIds);
        }
        return TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
    }

    private List<String> getTrackIds(Collection<Track> playlistContent) {
        List<String> trackIds = new ArrayList<>(playlistContent.size());
        for (Track track : playlistContent) {
            trackIds.add(track.getId());
        }
        return trackIds;
    }
}
