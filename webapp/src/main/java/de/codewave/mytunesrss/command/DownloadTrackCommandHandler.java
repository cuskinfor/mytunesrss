package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * de.codewave.mytunesrss.command.DownloadTrackCommandHandler
 */
public class DownloadTrackCommandHandler extends PlayTrackCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadTrackCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (!getAuthUser().isDownload()) {
            getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            super.executeAuthorized();
        }
    }
    
    @Override
    protected void updateStatisticsOnInitialRequest(String trackId, Track track) {
        // nothing to do here
    }

    @Override
    protected void setResponseHeaders(Track track, File file) {
        getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + MyTunesRssUtils.getLegalFileName(FilenameUtils.getBaseName(file.getName()) + "." + MyTunesFunctions.suffix(getWebConfig(), getAuthUser(), track, getBooleanRequestParameter("notranscode", false))) + "\"");
    }
}
