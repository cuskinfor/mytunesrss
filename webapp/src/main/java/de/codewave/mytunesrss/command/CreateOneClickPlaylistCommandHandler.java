package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.SaveMyTunesPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.CreateOneClickPlaylistCommandHandler
 */
public class CreateOneClickPlaylistCommandHandler extends AddToPlaylistCommandHandler {
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

    private List<String> getTrackIds(Collection<Track> playlistContent) {
        List<String> trackIds = new ArrayList<String>(playlistContent.size());
        for (Track track : playlistContent) {
            trackIds.add(track.getId());
        }
        return trackIds;
    }
}