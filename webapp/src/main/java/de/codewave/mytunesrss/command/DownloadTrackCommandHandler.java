package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.UpdatePlayCountAndDateStatement;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
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
        if (!isRequestAuthorized() || !getAuthUser().isDownload()) {
            getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else {
            StreamSender streamSender = null;
            Track track = null;
            String trackId = getRequest().getParameter("track");
            try {
                DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId}));
                if (tracks.getResultSize() > 0) {
                    track = tracks.nextResult();

                    File file = track.getFile();
                    if (file.exists()) {
                        getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + FilenameUtils.getBaseName(file.getName()) + "." + MyTunesFunctions.suffix(getRequest(), getWebConfig(), getAuthUser(), track) + "\"");
                        streamSender = MyTunesRssWebUtils.getMediaStreamSender(getRequest(), track, file);
                        getTransaction().executeStatement(new UpdatePlayCountAndDateStatement(new String[] {track.getId()}));
                        streamSender.setCounter(new MyTunesRssSendCounter(getAuthUser(), track.getId(), SessionManager.getSessionInfo(getRequest())));
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Requested file \"" + file.getAbsolutePath() + "\" does not exist.");
                        }
                        MyTunesRss.ADMIN_NOTIFY.notifyMissingFile(track);
                        streamSender = new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("No tracks recognized in request, sending response code SC_NOT_FOUND instead.");
                    }
                    streamSender = new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
                }
            } finally {
                getTransaction().commit();
            }
            if (ServletUtils.isHeadRequest(getRequest())) {
                sendHeadResponse(streamSender);
            } else {
                sendGetResponse(streamSender);
            }
        }
    }
}