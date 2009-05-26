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
        List<Track> currentPlaylist;
        DataStoreQuery.QueryResult<Track> tracks = getTracks();
        if (tracks == null || tracks.getResultSize() == 0) {
            currentPlaylist = MyTunesRss.QUICKTIME_PLAYER.getPlaylist();
            if (currentPlaylist.isEmpty()) {
                throw new IllegalArgumentException("No tracks found for request parameters!");
            }
        } else {
            currentPlaylist = tracks.getResults();
        }
        getRequest().setAttribute("tracks", currentPlaylist);
        forward(MyTunesRssResource.RemoteControl);
    }
}