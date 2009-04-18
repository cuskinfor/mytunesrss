package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * de.codewave.mytunesrss.command.ShowRemoteControlHandler
 */
public class ShowRemoteControlHandler extends CreatePlaylistBaseCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        DataStoreQuery.QueryResult<Track> tracks = getTracks();
        if (tracks.getResultSize() == 0) {
            throw new IllegalArgumentException("No tracks found for request parameters!");
        }
        getRequest().setAttribute("tracks", tracks.getResults());
        forward(MyTunesRssResource.RemoteControl);
    }
}