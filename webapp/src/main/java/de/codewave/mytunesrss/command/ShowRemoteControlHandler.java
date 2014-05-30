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
        MediaRendererController controller = MediaRendererController.getInstance();
        if (tracks == null || tracks.getResultSize() == 0) {
            if (controller.getPlaylist().isEmpty()) {
                forward(MyTunesRssCommand.ShowPortal);
                return; // done!
            }
        } else {
            controller.setTracks(getAuthUser(), tracks.getResults());
        }
        if (Boolean.valueOf(getRequestParameter("shuffle", "false"))) {
            controller.shuffle();
        }
        getRequest().setAttribute("tracks", controller.getPlaylist());
        getRequest().setAttribute("playlistVersion", controller.getPlaylistVersion());
        getRequest().setAttribute("itemsPerPage", 10); // TODO config
        getRequest().setAttribute("pagesPerPager", 10); // TODO config
        getRequest().setAttribute("currentPage", controller.getCurrentTrackInfo().getCurrentTrack() / 10); // TODO config
        String mediaRendererName = controller.getMediaRendererName();
        getRequest().setAttribute("currentlySelectedMediaRenderer", mediaRendererName.substring(0, Math.min(30, mediaRendererName.length())));
        forward(MyTunesRssResource.RemoteControl);
    }
}
