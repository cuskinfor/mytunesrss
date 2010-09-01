/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.jna.ffmpeg.HttpLiveStreamingSegmenter;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.UpdatePlayCountAndDateStatement;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.mytunesrss.transcoder.Transcoder;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HttpLiveStreamCommandHandler extends PlayTrackCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLiveStreamCommandHandler.class);

    @Override
    public void executeAuthorized() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        String[] pathInfo = StringUtils.split(getRequest().getPathInfo(), "/");
        if (pathInfo.length >= 2 && StringUtils.endsWithIgnoreCase(pathInfo[pathInfo.length - 1], ".ts")) {
            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.touch(pathInfo[pathInfo.length - 2]);
            sendSequenceFile(pathInfo[pathInfo.length - 1]);
        } else {
            synchronized (HttpLiveStreamCommandHandler.class) {
                String trackId = getRequest().getParameter("track");
                if (MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(trackId) != null) {
                    sendPlaylistFile(trackId);
                } else {
                    startSegmenter(trackId);
                    sendPlaylistFile(trackId);
                }
            }
        }
    }

    private void startSegmenter(final String cacheKey) throws SQLException, IOException {
        InputStream mediaStream = null;
        Track track = null;
        String trackId = getRequest().getParameter("track");
        DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId}));
        if (tracks.getResultSize() > 0) {
            track = tracks.nextResult();
            if (!getAuthUser().isQuotaExceeded()) {
                File file = track.getFile();
                if (!file.exists()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Requested file \"" + file.getAbsolutePath() + "\" does not exist.");
                    }
                    MyTunesRss.ADMIN_NOTIFY.notifyMissingFile(track);
                } else {
                    Transcoder transcoder = getTranscoder(track);
                    if (transcoder != null) {
                        transcoder.setTempFile(false);
                        mediaStream = transcoder.getStream();
                    } else {
                        mediaStream = new FileInputStream(file);
                    }
                    getTransaction().executeStatement(new UpdatePlayCountAndDateStatement(new String[]{track.getId()}));
                    getAuthUser().playLastFmTrack(track);
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("User limit exceeded, sending response code SC_NO_CONTENT instead.");
                }
                MyTunesRss.ADMIN_NOTIFY.notifyQuotaExceeded(getAuthUser());
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("No tracks recognized in request, sending response code SC_NO_CONTENT instead.");
            }
        }
        getTransaction().commit();
        String targetDir = MyTunesRssUtils.getCacheDataPath() + "/" + MyTunesRss.CACHEDIR_HTTPLIVESTREAMING;
        if (new File(targetDir).isDirectory()) {
            final HttpLiveStreamingSegmenter httpLiveStreamingSegmenter = new HttpLiveStreamingSegmenter(mediaStream, targetDir, UUID.randomUUID().toString(), 10);
            MyTunesRss.EXECUTOR_SERVICE.schedule(new Runnable() {
                public void run() {
                    try {
                        httpLiveStreamingSegmenter.run();
                    } finally {
                        if (httpLiveStreamingSegmenter.isFailed()) {
                            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.remove(cacheKey);
                        }
                    }
                }
            }, 0, TimeUnit.MILLISECONDS);
            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.add(new HttpLiveStreamingCacheItem(cacheKey, 3600000, httpLiveStreamingSegmenter));
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("HTTP Live Streaming directory \"" + targetDir + "\" did not exist and could not be created.");
            }
        }
    }

    private void sendSequenceFile(String filename) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending HTTP Live Streaming file \"" + filename + "\".");
        }
        File file = new File(MyTunesRssUtils.getCacheDataPath() + "/" + MyTunesRss.CACHEDIR_HTTPLIVESTREAMING + "/" + filename);
        new FileSender(file, "video/MP2T", (int) file.length()).sendGetResponse(getRequest(), getResponse(), false);
    }

    private void sendPlaylistFile(String trackId) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending HTTP Live Streaming playlist \"" + trackId + "\".");
        }
        HttpLiveStreamingCacheItem liveStreamingCacheItem = MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(trackId);
        if (liveStreamingCacheItem != null) {
            HttpLiveStreamingSegmenter segmenter = liveStreamingCacheItem.getSegmenter();
            // wait for segemnter to finish or at least one file appear
            while (!segmenter.isDone() && segmenter.getFiles().size() == 0) {
                Thread.sleep(500);
            }
            if (!segmenter.isFailed()) {
                String playlistString = segmenter.getPlaylist(trackId + "/{FILENAME}");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sending m3u8 playlist: " + playlistString);
                }

                byte[] playlist = playlistString.getBytes("ISO-8859-1");
                new StreamSender(new ByteArrayInputStream(playlist), "application/x-mpegURL", playlist.length).sendGetResponse(getRequest(), getResponse(), false);
            } else {
                MyTunesRss.HTTP_LIVE_STREAMING_CACHE.remove(trackId);
                new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
