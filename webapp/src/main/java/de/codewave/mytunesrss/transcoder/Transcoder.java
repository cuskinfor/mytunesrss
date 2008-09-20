package de.codewave.mytunesrss.transcoder;

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
    private boolean myTempFile;
    private boolean myPlayerRequest;
    private int myTargetBitrate;
    private int myTargetSampleRate;
    private boolean myActive;
    private Track myTrack;

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

    protected Transcoder(Track track, HttpServletRequest request, WebConfig webConfig) {
        myTrack = track;
        myPlayerRequest = "true".equalsIgnoreCase(request.getParameter("playerRequest"));
        myTempFile = (ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request) || !webConfig.isTranscodeOnTheFlyIfPossible()) &&
                !myPlayerRequest;
        myTargetBitrate = webConfig.getLameTargetBitrate();
        myTargetSampleRate = webConfig.getLameTargetSampleRate();
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
        final String identifier = myTrack.getId() + "_" + getTranscoderId();
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

    protected Track getTrack() {
        return myTrack;
    }

    protected void setTempFile(boolean tempFile) {
        myTempFile = tempFile;
    }

    public abstract String getTranscoderId();

    public abstract String getTargetContentType();

    public abstract InputStream getStream() throws IOException;

    public boolean isActive() {
        return myActive;
    }

    public boolean isAvailable() {
        return myTargetBitrate > 0 && myTargetSampleRate > 0;
    }

    protected int getTargetBitrate() {
        return myTargetBitrate;
    }

    protected int getTargetSampleRate() {
        return myTargetSampleRate;
    }
}