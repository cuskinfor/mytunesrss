package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.IOUtils;

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
    private Track myTrack;

    public static Transcoder createTranscoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        TranscoderConfig transcoderConfig = webConfig.getTranscoder(track);
        Transcoder transcoder = transcoderConfig != null ? new AudioTranscoder(transcoderConfig, track, webConfig, request) : null;
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
        File cacheDir = new File(MyTunesRssUtils.getCacheDataPath() + "/transcoder/cache");
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
            return new FileSender(transcodedFile, getTargetContentType(), (int) transcodedFile.length()) {
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

    protected abstract boolean isActive();

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