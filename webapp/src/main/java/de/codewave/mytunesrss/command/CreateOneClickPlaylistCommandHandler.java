package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SaveMyTunesPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.CreateOneClickPlaylistCommandHandler
 */
public class CreateOneClickPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter("user_private",
                    false));
            statement.setName(getRequestParameter("name", "new playlist"));
            statement.setTrackIds(getTrackIds(getTransaction().executeQuery(getQuery()).getResults()));
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

    protected DataStoreQuery<DataStoreQuery.QueryResult<Track>> getQuery() throws SQLException {
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = null;
        if (trackIds != null && trackIds.length > 0) {
            return FindTrackQuery.getForIds(trackIds);
        }
        return TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
    }

    private List<String> getTrackIds(Collection<Track> playlistContent) {
        List<String> trackIds = new ArrayList<String>(playlistContent.size());
        for (Track track : playlistContent) {
            trackIds.add(track.getId());
        }
        return trackIds;
    }
}