/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.UpdatePlayCountAndDateStatement;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingPlaylist;
import de.codewave.mytunesrss.transcoder.Transcoder;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.io.StreamCopyThread;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class HttpLiveStreamingCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpLiveStreamingCommandHandler.class);

    @Override
    public void executeAuthorized() throws IOException, SQLException {
        String trackId = getRequestParameter("track", null);
        if (StringUtils.isBlank(trackId)) {
            getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "missing track id");
        }
        String[] pathInfo = StringUtils.split(getRequest().getPathInfo(), '/');
        if (pathInfo.length > 1) {
            if (StringUtils.endsWithIgnoreCase(pathInfo[pathInfo.length - 1], ".ts")) {
                sendMediaFile(trackId, pathInfo[pathInfo.length - 1]);
            } else {
                sendPlaylist(trackId);
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendMediaFile(String trackId, String filename) throws IOException {
        StreamSender sender;
        MyTunesRss.HTTP_LIVE_STREAMING_CACHE.touch(trackId);
        File mediaFile = new File(getBaseDir(), filename);
        if (mediaFile.isFile()) {
            if (getAuthUser().isQuotaExceeded()) {
                sender = new PlayTrackCommandHandler.StatusCodeSender(HttpServletResponse.SC_FORBIDDEN);
            } else {
                sender = new FileSender(mediaFile, "video/MP2T", mediaFile.length());
                sender.setCounter(new MyTunesRssSendCounter(getAuthUser(), SessionManager.getSessionInfo(getRequest())));
            }
        } else {
            sender = new PlayTrackCommandHandler.StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
        }
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }

    private File getBaseDir() {
        try {
            return new File(MyTunesRssUtils.getCacheDataPath(), MyTunesRss.CACHEDIR_HTTP_LIVE_STREAMING);
        } catch (IOException e) {
            throw new RuntimeException("Could not get cache data path.");
        }
    }

    private void sendPlaylist(String trackId) throws SQLException, IOException {
        StreamSender sender;
        DataStoreQuery.QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId}));
        if (tracks.getResultSize() > 0) {
            Track track = tracks.nextResult();
            if (track.getMediaType() == MediaType.Video) {
                Transcoder transcoder = MyTunesRssWebUtils.getTranscoder(getRequest(), track);
                String playlistIdentifier = transcoder != null ? transcoder.getTranscoderId() : "";
                HttpLiveStreamingCacheItem cacheItem = MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(trackId);
                if (cacheItem == null) {
                    MyTunesRss.HTTP_LIVE_STREAMING_CACHE.putIfAbsent(new HttpLiveStreamingCacheItem(trackId, 3600000)); // TODO: timeout configuration?
                    cacheItem = MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(trackId);
                }
                HttpLiveStreamingPlaylist playlist = cacheItem.getPlaylist(playlistIdentifier);
                if (playlist == null && cacheItem.putIfAbsent(playlistIdentifier, new HttpLiveStreamingPlaylist())) {
                    InputStream mediaStream = MyTunesRssWebUtils.getMediaStream(getRequest(), track);
                    MyTunesRss.EXECUTOR_SERVICE.schedule(new HttpLiveStreamingSegmenterRunnable(cacheItem.getPlaylist(playlistIdentifier), mediaStream), 0, TimeUnit.MILLISECONDS);
                    MyTunesRss.HTTP_LIVE_STREAMING_CACHE.add(cacheItem);
                    getTransaction().executeStatement(new UpdatePlayCountAndDateStatement(new String[]{trackId}));
                    getTransaction().commit();
                    getAuthUser().playLastFmTrack(track);
                }
                playlist = cacheItem.getPlaylist(playlistIdentifier);
                // wait for at least 1 playlist item
                try {
                    long timeSlept = 0;
                    while (!playlist.isFailed() && !playlist.isDone() && playlist.getSize() == 0 && timeSlept < 30000) {
                        Thread.sleep(500);
                        timeSlept += 500;
                    }
                } catch (InterruptedException e) {
                    // we have been interrupted, so send the playlist file or an error now
                }
                if (playlist.isFailed() || playlist.getSize() == 0) {
                    cacheItem.removePlaylist(playlistIdentifier);
                    sender = new PlayTrackCommandHandler.StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    byte[] playlistBytes = playlist.getAsString().getBytes("ISO-8859-1");
                    sender = new StreamSender(new ByteArrayInputStream(playlistBytes), "application/x-mpegURL", playlistBytes.length);
                }

            } else {
                sender = new PlayTrackCommandHandler.StatusCodeSender(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            sender = new PlayTrackCommandHandler.StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
        }
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }

    public class HttpLiveStreamingSegmenterRunnable implements Runnable {

        private HttpLiveStreamingPlaylist myPlaylist;

        private InputStream myStream;

        public HttpLiveStreamingSegmenterRunnable(HttpLiveStreamingPlaylist playlist, InputStream stream) {
            myPlaylist = playlist;
            myStream = stream;
        }

        public void run() {
            String[] command = new String[6];
            command[0] = getJavaExecutablePath();
            try {
                command[1] = "-Djna.library.path=" + MyTunesRssUtils.getPreferencesDataPath() + "/lib";
            } catch (IOException e) {
                throw new RuntimeException("Could not get prefs data path.", e);
            }
            command[2] = "-cp";
            command[3] = getClasspath();
            command[4] = "de.codewave.jna.ffmpeg.HttpLiveStreamingSegmenter";
            command[5] = getBaseDir().getAbsolutePath();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing HTTP Live Streaming command \"" + StringUtils.join(command, " ") + "\".");
            }
            BufferedReader reader = null;
            try {
                Process process = Runtime.getRuntime().exec(command);
                new LogStreamCopyThread(process.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug).start();
                new StreamCopyThread(myStream, true, process.getOutputStream(), true).start();
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                for (String responseLine = reader.readLine(); responseLine != null; responseLine = reader.readLine()) {
                    if (responseLine.startsWith(getBaseDir().getAbsolutePath())) {
                        myPlaylist.addFile(new File(StringUtils.trimToEmpty(responseLine)));
                    } else if (responseLine.startsWith("ERR")) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("HTTP Live Streaming segmenter error: " + responseLine);
                        }
                    }
                }
                process.waitFor();
                if (process.exitValue() == 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Segmenter process exited with code 0.");
                    }
                    myPlaylist.setDone(true);
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Segmenter process exited with code " + process.exitValue() + ".");
                    }
                    myPlaylist.setFailed(true);
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Segmenter exception", e);
                }
                myPlaylist.setFailed(true);
            } catch (InterruptedException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Segmenter thread interrupted exception", e);
                }
                myPlaylist.setFailed(true);
            } finally {
                IOUtils.closeQuietly(reader);
            }
        }

        private String getJavaExecutablePath() {
            return System.getProperty("java.home") + "/bin/java";
        }

        private String getClasspath() {
            StringBuilder sb = new StringBuilder();
            for (String cpElement : StringUtils.split(System.getProperty("java.class.path"), System.getProperty("path.separator"))) {
                if (!cpElement.startsWith(System.getProperty("java.home"))) {
                    sb.append(cpElement).append(System.getProperty("path.separator"));
                }
            }
            return sb.substring(0, sb.length() - 1);
        }
    }
}

