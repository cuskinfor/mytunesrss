package de.codewave.mytunesrss.command;

import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.DownloadTrackCommandHandler
 */
public class DownloadTrackCommandHandler extends PlayTrackCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (!isRequestAuthorized() || !getAuthUser().isDownload()) {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            String trackId = getRequest().getParameter("track");
            DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId}));
            if (tracks.getResultSize() > 0) {
                Track track = tracks.nextResult();
                getResponse().setHeader("Content-Disposition", "attachment; filename=" + track.getFilename());
            }
            super.executeAuthorized();
        }
    }
}