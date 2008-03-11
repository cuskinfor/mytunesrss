package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public abstract class Transcoder {
    private String myTrackId;
    private File myFile;
    private boolean myTempFile;
    private boolean myPlayerRequest;
    private int myTargetBitrate;
    private int myTargetSampleRate;
    private boolean myActive;

    public static Transcoder createTranscoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        Transcoder transcoder = null;
        if (FileSupportUtils.isMp3(track.getFile())) {
            transcoder = new Mp3ToMp3Transcoder(track, webConfig, request);
        } else if (FileSupportUtils.isMp4(track.getFile())) {
            if ("alac".equals(track.getMp4Codec())) {
                transcoder = new AlacToMp3Transcoder(track, webConfig, request);
            } else {
                transcoder = new Mp4aToMp3Transcoder(track, webConfig, request);
            }
        }
        return transcoder != null && transcoder.isAvailable() && transcoder.isActive() ? transcoder : null;
    }

    protected Transcoder(String trackId, File file, HttpServletRequest request, WebConfig webConfig) {
        myPlayerRequest = "true".equalsIgnoreCase(request.getParameter("playerRequest"));
        myTempFile = (ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request) || !webConfig.isTranscodeOnTheFlyIfPossible()) && !myPlayerRequest;
        myTrackId = trackId;
        myFile = file;
        myTargetBitrate = webConfig.getLameTargetBitrate();
        myTargetSampleRate = webConfig.getLameTargetSampleRate();
        if (StringUtils.isNotEmpty(request.getParameter("tc"))) {
            String[] splitted = request.getParameter("tc").split(",");
            if (splitted.length == 3) {
                myActive = true;
                myTargetBitrate = Integer.parseInt(splitted[0]);
                myTargetSampleRate = Integer.parseInt(splitted[1]);
                setTempFileRequested(ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request) || !Boolean.parseBoolean(splitted[2]));
            }
        }
    }

    protected void setTempFileRequested(boolean tempFile) {
        myTempFile = !myPlayerRequest && tempFile;
    }

    public File getTranscodedFile() throws IOException {
        File cacheDir = new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/transcoder/cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File file = File.createTempFile("mytunesrss_", ".tmp", cacheDir);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        InputStream inputStream = getStream();
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
        return file;
    }

    public StreamSender getStreamSender() throws IOException {
        final String identifier = myTrackId + "_" + getTranscoderId();
        if (myTempFile) {
            File transcodedFile = MyTunesRss.STREAMING_CACHE.lock(identifier);
            if (transcodedFile == null) {
                transcodedFile = getTranscodedFile();
                MyTunesRss.STREAMING_CACHE.add(identifier, transcodedFile, MyTunesRss.CONFIG.getStreamingCacheTimeout() * 60000);
                MyTunesRss.STREAMING_CACHE.lock(identifier);
            }
            return new FileSender(transcodedFile, getTargetContentType(), (int)transcodedFile.length()) {
                protected void afterSend() {
                    MyTunesRss.STREAMING_CACHE.unlock(identifier);
                }
            };
        } else {
            return new StreamSender(getStream(), getTargetContentType(), 0);
        }
    }

    protected File getFile() {
        return myFile;
    }

    protected void setTempFile(boolean tempFile) {
        myTempFile = tempFile;
    }

    protected abstract String getTranscoderId();

    public abstract String getTargetContentType();

    public abstract InputStream getStream() throws IOException;

    public boolean isActive() {
        return myActive;
    }

    public boolean isAvailable() {
        return MyTunesRss.REGISTRATION.isRegistered() && myTargetBitrate > 0 && myTargetSampleRate > 0;
    }

    protected int getTargetBitrate() {
        return myTargetBitrate;
    }

    protected int getTargetSampleRate() {
        return myTargetSampleRate;
    }
}