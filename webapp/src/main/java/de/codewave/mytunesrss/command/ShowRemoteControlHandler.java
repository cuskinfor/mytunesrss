package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;

import java.util.List;

/**
 * de.codewave.mytunesrss.command.ShowRemoteControlHandler
 */
public class ShowRemoteControlHandler extends CreatePlaylistBaseCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        DataStoreQuery.QueryResult<Track> tracks = getTracks();
        if (tracks == null || tracks.getResultSize() == 0) {
            if (MyTunesRss.QUICKTIME_PLAYER.getPlaylist().isEmpty()) {
                throw new IllegalArgumentException("No tracks found for request parameters!");
            }
        } else {
            MyTunesRss.QUICKTIME_PLAYER.setTracks(tracks.getResults());
        }
        if (Boolean.valueOf(getRequestParameter("shuffle", "false"))) {
            MyTunesRss.QUICKTIME_PLAYER.shuffle();
        }
        getRequest().setAttribute("tracks", MyTunesRss.QUICKTIME_PLAYER.getPlaylist());
        forward(MyTunesRssResource.RemoteControl);
    }
}