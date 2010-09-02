/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.UpdatePlayCountAndDateStatement;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class HttpLiveStreamingCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLiveStreamingCommandHandler.class);

    @Override
    public void executeAuthorized() throws IOException, SQLException {
        if (!MyTunesRss.CONFIG.isValidHttpLiveStreamingBinary()) {
            getResponse().sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "missing http live streaming configuration");
        }
        String trackId = getRequestParameter("track", null);
        if (StringUtils.isBlank(trackId)) {
            getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "missing track id");
        }
        String[] pathInfo = StringUtils.split(getRequest().getPathInfo(), '/');
        if (pathInfo.length > 1) {
            if (StringUtils.endsWithIgnoreCase(pathInfo[pathInfo.length - 1], ".m3u8")) {
                sendPlaylist(trackId);
            } else if (StringUtils.endsWithIgnoreCase(pathInfo[pathInfo.length - 1], ".ts")) {
                sendMediaFile(trackId, pathInfo[pathInfo.length - 1]);
            } else {
                getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendMediaFile(String trackId, String filename) throws IOException {
        HttpLiveStreamingCacheItem cacheItem = MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(trackId);
        if (cacheItem.isFailed()) {
            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.remove(trackId);
            cacheItem = null;
        }
        if (cacheItem == null) {
            getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "media file not found");
        } else {
            File basedir = new File(MyTunesRssUtils.getCacheDataPath(), MyTunesRss.CACHEDIR_HTTP_LIVE_STREAMING);
            File mediaFile = new File(basedir, filename);
            if (mediaFile.isFile()) {
                if (getAuthUser().isQuotaExceeded()) {
                    getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    FileSender fileSender = new FileSender(mediaFile, "video/MP2T", mediaFile.length());
                    fileSender.setCounter(new MyTunesRssSendCounter(getAuthUser(), SessionManager.getSessionInfo(getRequest())));
                    fileSender.sendGetResponse(getRequest(), getResponse(), false);
                }
            } else {
                getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "media file not found");
            }
        }
    }

    private void sendPlaylist(String trackId) throws SQLException, IOException {
        HttpLiveStreamingCacheItem cacheItem = MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(trackId);
        if (cacheItem == null) {
            DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId}));
            if (tracks.getResultSize() > 0) {
                Track track = tracks.nextResult();
                if (track.getMediaType() == MediaType.Video) {
                    cacheItem = new HttpLiveStreamingCacheItem(trackId, 3600000); // TODO: timeout configuration?
                    MyTunesRss.EXECUTOR_SERVICE.schedule(new HttpLiveStreamingSegmenterRunnable(cacheItem, trackId), 0, TimeUnit.MILLISECONDS);
                    MyTunesRss.HTTP_LIVE_STREAMING_CACHE.add(cacheItem);
                    getTransaction().executeStatement(new UpdatePlayCountAndDateStatement(new String[]{trackId}));
                    getAuthUser().playLastFmTrack(track);
                } else {
                    getResponse().sendError(HttpServletResponse.SC_FORBIDDEN, "requested track is not a video");
                    return;
                }
            } else {
                getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "track not found");
                return;
            }
        }
        // wait for at least 3 playlist items
        try {
            while (!cacheItem.isFailed() && !cacheItem.isDone() && cacheItem.getPlaylistSize() < 3) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            // we have been interrupted, so send the playlist file or an error now
        }
        if (cacheItem.isFailed()) {
            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.remove(trackId);
            getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "playlist file not found");
        } else {
            byte[] playlistBytes = cacheItem.getPlaylist().getBytes("UTF-8");
            getResponse().setContentType("application/x-mpegURL; charset=UTF-8");
            getResponse().setContentLength(playlistBytes.length);
            getResponse().getOutputStream().write(playlistBytes);
        }
    }

    public static class HttpLiveStreamingSegmenterRunnable implements Runnable {

        private HttpLiveStreamingCacheItem myCacheItem;

        private String myFile;

        public HttpLiveStreamingSegmenterRunnable(HttpLiveStreamingCacheItem cacheItem, String file) {
            myCacheItem = cacheItem;
            myFile = file;
        }

        public void run() {
            String liveStreamingOptions = MyTunesRss.CONFIG.getHttpLiveStreamingOptions();
            String[] transcoderCommand = new String[liveStreamingOptions.split(" ").length + 1];
            transcoderCommand[0] = MyTunesRss.CONFIG.getHttpLiveStreamingBinary();
            int i = 1;
            for (String part : liveStreamingOptions.split(" ")) {
                transcoderCommand[i++] = part.replace("{infile}", myFile);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("executing HTTP Live Streaming command \"" + StringUtils.join(transcoderCommand, " ") + "\".");
            }
            BufferedReader reader = null;
            try {
                Process process = Runtime.getRuntime().exec(transcoderCommand);
                new LogStreamCopyThread(process.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug).start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                for (String responseLine = reader.readLine(); responseLine != null; responseLine = reader.readLine()) {
                    if (responseLine.startsWith("segmenter_file:")) {
                        myCacheItem.addFile(new File(StringUtils.trimToEmpty(responseLine.substring("segmenter_file:".length()))));
                    } else if ("segmenter_done".equals(responseLine)) {
                        myCacheItem.setDone(true);
                    }
                }
            } catch (IOException e) {
                myCacheItem.setFailed(true);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }
    }
}
