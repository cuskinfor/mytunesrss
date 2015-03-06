/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.RedirectSender;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamListener;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.PlayTrackCommandHandler
 */
public class PlayTrackCommandHandler extends BandwidthThrottlingCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PlayTrackCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        StreamSender streamSender;
        String trackId = getRequest().getParameter("track");
        boolean initialRequest = getBooleanRequestParameter("initial", true);
        try {
            QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[] {trackId}));
            if (tracks.getResultSize() > 0) {
                final Track track = tracks.nextResult();
                if (initialRequest && getAuthUser().isQuotaExceeded()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("User limit exceeded, sending response code SC_CONFLICT instead.");
                    }
                    MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(getAuthUser());
                    streamSender = new StatusCodeSender(HttpServletResponse.SC_CONFLICT, "QUOTA_EXCEEDED");
                } else {
                    File file = track.getFile();
                    if (!file.exists()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Requested file \"" + MiscUtils.getUtf8UrlEncoded(file.getAbsolutePath()) + "\" does not exist.");
                        }
                        MyTunesRss.ADMIN_NOTIFY.notifyMissingFile(track);
                        streamSender = new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        if (initialRequest) {
                            streamSender = handleInitialRequest(track, file);
                        } else {
                            streamSender = handleFollowUpRequest(track, file);
                        }
                    }
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

    protected StreamSender handleInitialRequest(final Track track, File file) {
        StreamSender streamSender = new RedirectSender(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.PlayTrack) + "/" + MyTunesRssUtils.encryptPathInfo(getRequest().getPathInfo().replace("/" + MyTunesRssCommand.PlayTrack.getName(), "initial=false")));
        MyTunesRssUtils.asyncPlayCountAndDateUpdate(track.getId());
        getAuthUser().playLastFmTrack(track);
        streamSender.setStreamListener(new StreamListener() {
            @Override
            public void afterSend() {
                StatisticsEventManager.getInstance().fireEvent(new DownloadEvent(getAuthUser().getName(), track.getId(), 0));
            }
        });
        return streamSender;
    }

    protected StreamSender handleFollowUpRequest(Track track, File file) throws IOException {
        StreamSender streamSender = MyTunesRssWebUtils.getMediaStreamSender(getRequest(), track, file);
        streamSender.setCounter(new MyTunesRssSendCounter(getAuthUser(), SessionManager.getSessionInfo(getRequest())));
        return streamSender;
    }

    @Override
    protected void executeUnauthorized() throws IOException, ServletException {
        getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    protected void sendGetResponse(StreamSender streamSender) throws IOException {
        streamSender.sendGetResponse(getRequest(), getResponse(), false);
    }

    protected void sendHeadResponse(StreamSender streamSender) {
        streamSender.sendHeadResponse(getRequest(), getResponse());
    }

}
