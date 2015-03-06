/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssSendCounter;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.RedirectSender;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamListener;
import de.codewave.utils.servlet.StreamSender;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class HttpLiveStreamingCommandHandler extends BandwidthThrottlingCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLiveStreamingCommandHandler.class);

    @Override
    public void executeAuthorized() throws IOException, SQLException {
        String[] pathInfo = StringUtils.split(getRequest().getPathInfo(), '/');
        if (pathInfo.length > 1) {
            if (StringUtils.endsWithIgnoreCase(pathInfo[pathInfo.length - 1], ".ts")) {
                sendMediaFile(pathInfo[pathInfo.length - 2], pathInfo[pathInfo.length - 1]);
            } else {
                String trackId = getRequestParameter("track", null);
                if (StringUtils.isBlank(trackId)) {
                    getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "missing track id");
                } else {
                    sendPlaylist(trackId);
                }
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendMediaFile(String trackId, String filename) throws IOException {
        StreamSender sender;
        MyTunesRss.HTTP_LIVE_STREAMING_CACHE.touch(trackId);
        File mediaFile = new File(MyTunesRss.HTTP_LIVE_STREAMING_CACHE.getBaseDir(), trackId + "/" + filename);
        if (mediaFile.isFile()) {
            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.touch(trackId);
            if (getAuthUser().isQuotaExceeded()) {
                LOGGER.debug("Sending 409 QUOTA_EXCEEDED response.");
                sender = new StatusCodeSender(HttpServletResponse.SC_CONFLICT, "QUOTA_EXCEEDED");
            } else {
                sender = new FileSender(mediaFile, "video/MP2T", mediaFile.length());
                LOGGER.debug("Sending video/MP2T response \"" + mediaFile.getAbsolutePath() + "\".");
                sender.setCounter(new MyTunesRssSendCounter(getAuthUser(), SessionManager.getSessionInfo(getRequest())));
            }
        } else {
            LOGGER.debug("Sending 404 NOT_FOUND response.");
            sender = new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
        }
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }

    private void sendPlaylist(String trackId) throws SQLException, IOException {
        StreamSender sender;
        QueryResult<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForIds(new String[]{trackId}));
        if (tracks.getResultSize() > 0) {
            final Track track = tracks.nextResult();
            if (track.getMediaType() == MediaType.Video) {
                if (getBooleanRequestParameter("initial", true)) {
                    MyTunesRssUtils.asyncPlayCountAndDateUpdate(trackId);
                    getAuthUser().playLastFmTrack(track);
                    sender = new RedirectSender(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.HttpLiveStream) + "/" + MyTunesRssUtils.encryptPathInfo(getRequest().getPathInfo().replace(MyTunesRssCommand.HttpLiveStream.getName(), "initial=false")));
                    sender.setStreamListener(new StreamListener() {
                        @Override
                        public void afterSend() {
                            StatisticsEventManager.getInstance().fireEvent(new DownloadEvent(getAuthUser().getName(), track.getId(),  0));
                        }
                    });
                } else {
                    File dir = new File(MyTunesRss.HTTP_LIVE_STREAMING_CACHE.getBaseDir(), trackId);
                    if (!dir.isDirectory()) {
                        synchronized (this) {
                            if (!dir.isDirectory()) {
                                if (!dir.mkdir()) {
                                    LOGGER.warn("Could not create folder for http live streaming.");
                                }
                                MyTunesRss.EXECUTOR_SERVICE.execute(new HttpLiveStreamingSegmenterRunnable(dir, track.getFile()));
                            }
                        }
                    }
                    File playlistFile = waitForPlaylistFile(dir, 30000);
                    MyTunesRss.HTTP_LIVE_STREAMING_CACHE.touch(trackId);
                    byte[] playlistBytes = FileUtils.readFileToByteArray(playlistFile);
                    LOGGER.debug("Sending playlist: " + new String(playlistBytes, Charset.forName("UTF-8")));
                    sender = new StreamSender(new ByteArrayInputStream(playlistBytes), "application/x-mpegURL", playlistBytes.length);
                }
            } else {
                sender = new StatusCodeSender(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            sender = new StatusCodeSender(HttpServletResponse.SC_NOT_FOUND);
        }
        sender.sendGetResponse(getRequest(), getResponse(), false);
    }

    private File waitForPlaylistFile(File dir, long timeoutMillis) throws IOException {
        LOGGER.debug("Waiting up to " + timeoutMillis + " milliseconds for playlist file.");
        File playlistFile = new File(dir, "playlist.m3u8");
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            LOGGER.debug("Reading playlist file \"" + playlistFile.getAbsolutePath() + "\".");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(playlistFile), "UTF-8"))) {
                int segments = 0;
                for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                    LOGGER.debug("Read line \"" + line + "\" from playlistFile file.");
                    if (StringUtils.trimToEmpty(StringUtils.lowerCase(line)).startsWith("#extinf")) {
                        LOGGER.debug("Found segment " + (segments + 1) + ".");
                        segments++;
                        if (segments == 3) {
                            LOGGER.debug("Enough segments found, returning playlist file after " + (System.currentTimeMillis() - startTime) + " milliseconds.");
                            return playlistFile;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.debug("Caught IOException while waiting for playlist file.", e);
            } finally {
                LOGGER.debug("Closing playlist file reader.");

            }
            try {
                LOGGER.debug("Sleeping a while before trying again to read playlist file.");
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                LOGGER.debug("Interrupted while waiting for file.");
            }
        }
        throw new IOException("Timeout waiting for playlist file.");
    }

    public static class HttpLiveStreamingSegmenterRunnable implements Runnable {

        private File myTargetDir;

        private File myVideoFile;

        public HttpLiveStreamingSegmenterRunnable(File targetDir, File videoFile) {
            myTargetDir = targetDir;
            myVideoFile = videoFile;
        }

        @Override
        public void run() {
            Process process = null;
            try {
                List<String> transcodeCommand = MyTunesRssUtils.getDefaultVlcCommand(myVideoFile);
                transcodeCommand.add("--no-sout-smem-time-sync");
                transcodeCommand.add("--sout=#transcode{height=320,canvas-aspect=1.5:1,vb=768,vcodec=h264,venc=x264{aud,profile=baseline,level=30,keyint=30,bframes=0,ref=1,nocabac},acodec=mp3,ab=128,samplerate=44100,channels=2,deinterlace,audio-sync}:std{access=livehttp{seglen=10,index=" + myTargetDir.getAbsolutePath() + "/playlist.m3u8" + ",index-url=./" + myTargetDir.getName() + "/stream-########.ts},mux=ts{use-key-frames},dst=" + myTargetDir.getAbsolutePath() + "/stream-########.ts}");
                String msg = "Executing HTTP Live Streaming command \"" + StringUtils.join(transcodeCommand, " ") + "\".";
                LOGGER.debug(msg);
                process = new ProcessBuilder(transcodeCommand).start();
                MyTunesRss.SPAWNED_PROCESSES.add(process);
                LogStreamCopyThread stdoutCopyThread = new LogStreamCopyThread(process.getInputStream(), false, LoggerFactory.getLogger("VLC"), LogStreamCopyThread.LogLevel.Info, msg, null);
                stdoutCopyThread.setDaemon(true);
                stdoutCopyThread.start();
                LogStreamCopyThread stderrCopyThreads = new LogStreamCopyThread(process.getErrorStream(), false, LoggerFactory.getLogger("VLC"), LogStreamCopyThread.LogLevel.Error, msg, null);
                stderrCopyThreads.setDaemon(true);
                stderrCopyThreads.start();
                process.waitFor();
            } catch (Exception e) {
                LOGGER.error("Error in http live streaming thread.", e);
                try {
                    LOGGER.info("Trying to remove directory with incomplete segments from cache.");
                    FileUtils.deleteDirectory(myTargetDir);
                } catch (IOException ignored) {
                    LOGGER.error("Could not delete directory with incomplete segments. Trying to rename directory.");
                    if (!myTargetDir.renameTo(new File(myTargetDir.getParentFile(), UUID.randomUUID().toString()))) {
                        LOGGER.error("Could not rename directory with incomplete segments either. Please consider deleting caches manually as soon as possible.");
                    }
                }
            } finally {
                if (process != null) {
                    process.destroy();
                    MyTunesRss.SPAWNED_PROCESSES.remove(process);
                }
            }
        }
    }
}

