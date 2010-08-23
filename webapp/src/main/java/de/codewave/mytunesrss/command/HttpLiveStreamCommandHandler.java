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
import de.codewave.mytunesrss.transcoder.Transcoder;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpLiveStreamCommandHandler extends PlayTrackCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLiveStreamCommandHandler.class);

    public static ConcurrentHashMap<String, HttpLiveStreamingSegmenter> SEGMENTS = new ConcurrentHashMap<String, HttpLiveStreamingSegmenter>();

    @Override
    public void executeAuthorized() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        String pathInfo = getRequest().getPathInfo();
        int lastSlash = StringUtils.lastIndexOf(pathInfo, '/');
        String filename = StringUtils.substring(pathInfo, lastSlash + 1);
        if (filename.toLowerCase(Locale.US).endsWith(".ts")) {
            sendSequenceFile(filename);
        } else {
            synchronized (HttpLiveStreamCommandHandler.class) {
                String trackId = getRequest().getParameter("track");
                if (SEGMENTS.containsKey(trackId)) {
                    sendPlaylistFile(trackId);
                } else {
                    startSegmenter(trackId);
                    sendPlaylistFile(trackId);
                }
            }
        }
    }

    private void startSegmenter(String key) throws SQLException, IOException {
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
        HttpLiveStreamingSegmenter httpLiveStreamingSegmenter = new HttpLiveStreamingSegmenter(mediaStream, MyTunesRssUtils.getCacheDataPath() + "/httplivestream", UUID.randomUUID().toString(), 10);
        MyTunesRss.EXECUTOR_SERVICE.schedule(httpLiveStreamingSegmenter, 0, TimeUnit.MILLISECONDS);
        HttpLiveStreamCommandHandler.SEGMENTS.put(key, httpLiveStreamingSegmenter);
    }

    private void sendSequenceFile(String filename) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending HTTP Live Streaming file \"" + filename + "\".");
        }
        File file = new File(MyTunesRssUtils.getCacheDataPath() + "/httplivestream/" + filename);
        new FileSender(file, "video/MP2T", (int) file.length()).sendGetResponse(getRequest(), getResponse(), false);
    }

    private void sendPlaylistFile(String uuid) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending HTTP Live Streaming playlist \"" + uuid + "\".");
        }
        String playlistString = SEGMENTS.get(uuid).getPlaylist();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending m3u8 playlist: " + playlistString);
        }

        byte[] playlist = playlistString.getBytes("ISO-8859-1");
        new StreamSender(new ByteArrayInputStream(playlist), "application/x-mpegURL", playlist.length).sendGetResponse(getRequest(), getResponse(), false);
    }
}
