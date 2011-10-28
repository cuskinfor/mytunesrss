/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.camel.mp3.Id3Tag;
import de.codewave.camel.mp3.Id3v2Tag;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.UpdatePlayCountAndDateStatement;
import de.codewave.mytunesrss.transcoder.Transcoder;
import de.codewave.utils.servlet.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * de.codewave.mytunesrss.command.PlayTrackCommandHandler
 */
public class PlayTrackCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PlayTrackCommandHandler.class);

    @Override
    protected DataStoreSession getTransaction() {
        return super.getTransaction();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void executeAuthorized() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        StreamSender streamSender;
        Track track = null;
        String trackId = getRequest().getParameter("track");
        DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[] {trackId}));
        if (tracks.getResultSize() > 0) {
            track = tracks.nextResult();
            if (!getAuthUser().isQuotaExceeded()) {
                File file = track.getFile();
                String contentType = track.getContentType();
                if (!file.exists()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Requested file \"" + file.getAbsolutePath() + "\" does not exist.");
                    }
                    MyTunesRss.ADMIN_NOTIFY.notifyMissingFile(track);
                    streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    if (Mp4Utils.isMp4File(file)) {
                        // qt-faststart
                        LOG.info("Using QT-FASTSTART utility.");
                        streamSender = MyTunesRssWebUtils.getMediaStreamSender(getRequest(), track, Mp4Utils.getFastStartInputStream(file));
                    } else {
                        streamSender = MyTunesRssWebUtils.getMediaStreamSender(getRequest(), track, new FileInputStream(file));
                    }
                    getTransaction().executeStatement(new UpdatePlayCountAndDateStatement(new String[] {track.getId()}));
                    streamSender.setCounter(new MyTunesRssSendCounter(getAuthUser(), SessionManager.getSessionInfo(getRequest())));
                    getAuthUser().playLastFmTrack(track);
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("User limit exceeded, sending response code SC_CONFLICT instead.");
                }
                MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(getAuthUser());
                streamSender = new StatusCodeSender(HttpServletResponse.SC_CONFLICT, "QUOTA_EXCEEDED");
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No tracks recognized in request, sending response code SC_NO_CONTENT instead.");
            }
            streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
        }
        getTransaction().commit();
        if (ServletUtils.isHeadRequest(getRequest())) {
            sendHeadResponse(streamSender);
        } else {
            sendGetResponse(streamSender);
        }
    }

    protected void sendGetResponse(StreamSender streamSender) throws IOException {
        streamSender.sendGetResponse(getRequest(), getResponse(), false);
    }

    protected void sendHeadResponse(StreamSender streamSender) {
        streamSender.sendHeadResponse(getRequest(), getResponse());
    }

}