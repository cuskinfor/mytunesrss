package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;

/**
 * de.codewave.mytunesrss.command.ShowRemoteControlHandler
 */
public class ShowRemoteControlHandler extends CreatePlaylistBaseCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        DataStoreQuery.QueryResult<Track> tracks = getTracks();
        if (tracks == null || tracks.getResultSize() == 0) {
            if (MyTunesRss.VLC_PLAYER.getPlaylist().isEmpty()) {
                throw new IllegalArgumentException("No tracks found for request parameters!");
            }
        } else {
            MyTunesRss.VLC_PLAYER.setTracks(tracks.getResults());
        }
        if (Boolean.valueOf(getRequestParameter("shuffle", "false"))) {
            MyTunesRss.VLC_PLAYER.shuffle();
        }
        getRequest().setAttribute("tracks", MyTunesRss.VLC_PLAYER.getPlaylist());
        getRequest().setAttribute("itemsPerPage", 10); // TODO config
        getRequest().setAttribute("pagesPerPager", 10); // TODO config
        getRequest().setAttribute("currentPage", MyTunesRss.VLC_PLAYER.getCurrentIndex() / 10); // TODO config
        forward(MyTunesRssResource.RemoteControl);
    }
}