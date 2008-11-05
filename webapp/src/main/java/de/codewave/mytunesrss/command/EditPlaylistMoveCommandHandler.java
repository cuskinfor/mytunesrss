package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import java.util.List;

/**
 * de.codewave.mytunesrss.command.MoveTrackCommandHandler
 */
public class EditPlaylistMoveCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        int offset = getIntegerRequestParameter("offset", 0);
        int index = getIntegerRequestParameter("index", 0);
        int move = getIntegerRequestParameter("move", 0);
        List<Track> playlist = (List<Track>)getSession().getAttribute("playlistContent");
        if (playlist != null && !playlist.isEmpty()) {
            MyTunesRssWebUtils.movePlaylistTracks(playlist, getWebConfig().getEffectivePageSize() * offset + index, 1, move);
            getRequest().setAttribute("track", playlist.get(getWebConfig().getEffectivePageSize() * offset + index));
            forward(MyTunesRssResource.EditPlaylistMoveResponse);
        } else {
            addError(new BundleError("error.cannotEditEmptyPlaylist"));
            redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
        }
    }
}