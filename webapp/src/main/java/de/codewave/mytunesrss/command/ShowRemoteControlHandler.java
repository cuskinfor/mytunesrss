package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.mediarenderercontrol.MediaRendererController;
import de.codewave.utils.sql.QueryResult;

/**
 * de.codewave.mytunesrss.command.ShowRemoteControlHandler
 */
public class ShowRemoteControlHandler extends CreatePlaylistBaseCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        QueryResult<Track> tracks = getTracks();
        if (tracks == null || tracks.getResultSize() == 0) {
            if (MediaRendererController.getInstance().getPlaylist().isEmpty()) {
                throw new IllegalArgumentException("No tracks found for request parameters!");
            }
        } else {
            MediaRendererController.getInstance().setTracks(getAuthUser(), tracks.getResults());
        }
        if (Boolean.valueOf(getRequestParameter("shuffle", "false"))) {
            MediaRendererController.getInstance().shuffle();
        }
        getRequest().setAttribute("tracks", MediaRendererController.getInstance().getPlaylist());
        getRequest().setAttribute("itemsPerPage", 10); // TODO config
        getRequest().setAttribute("pagesPerPager", 10); // TODO config
        getRequest().setAttribute("currentPage", MediaRendererController.getInstance().getCurrentTrackInfo().getCurrentTrack() / 10); // TODO config
        String mediaRendererName = MediaRendererController.getInstance().getMediaRendererName();
        getRequest().setAttribute("currentlySelectedMediaRenderer", mediaRendererName.substring(0, Math.min(30, mediaRendererName.length())));
        forward(MyTunesRssResource.RemoteControl);
    }
}
