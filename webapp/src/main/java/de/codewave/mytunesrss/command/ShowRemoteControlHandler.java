package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.remotecontrol.MediaRendererRemoteController;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;

/**
 * de.codewave.mytunesrss.command.ShowRemoteControlHandler
 */
public class ShowRemoteControlHandler extends CreatePlaylistBaseCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        QueryResult<Track> tracks = getTracks();
        if (tracks == null || tracks.getResultSize() == 0) {
            if (MediaRendererRemoteController.getInstance().getPlaylist().isEmpty()) {
                throw new IllegalArgumentException("No tracks found for request parameters!");
            }
        } else {
            MediaRendererRemoteController.getInstance().setTracks(tracks.getResults());
        }
        if (Boolean.valueOf(getRequestParameter("shuffle", "false"))) {
            MediaRendererRemoteController.getInstance().shuffle();
        }
        getRequest().setAttribute("tracks", MediaRendererRemoteController.getInstance().getPlaylist());
        getRequest().setAttribute("itemsPerPage", 10); // TODO config
        getRequest().setAttribute("pagesPerPager", 10); // TODO config
        getRequest().setAttribute("currentPage", MediaRendererRemoteController.getInstance().getCurrentTrackInfo().getCurrentTrack() / 10); // TODO config
        forward(MyTunesRssResource.RemoteControl);
    }
}