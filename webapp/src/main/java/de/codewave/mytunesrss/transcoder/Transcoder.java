package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * de.codewave.mytunesrss.transcoder.Transcoder
 */
public class Transcoder {
    private boolean myTempFile;
    private boolean myPlayerRequest;
    private Track myTrack;
    private boolean myActive;
    private TranscoderConfig myTranscoderConfig;

    public static Transcoder createTranscoder(Track track, User user, WebConfig webConfig, HttpServletRequest request) {
        TranscoderConfig transcoderConfig = user.getTranscoder(track);
        Transcoder transcoder = transcoderConfig != null ? new Transcoder(transcoderConfig, track, request) : null;
        if (transcoder == null) {
            transcoderConfig = webConfig.getTranscoder(track);
            transcoder = transcoderConfig != null ? new Transcoder(transcoderConfig, track, webConfig, request) : null;
        }
        return transcoder != null && transcoder.isAvailable() && transcoder.isActive() ? transcoder : null;
    }

    protected Transcoder(TranscoderConfig transcoderConfig, Track track, HttpServletRequest request) {
        myTrack = track;
        myPlayerRequest = "true".equalsIgnoreCase(request.getParameter("playerRequest"));
        myTempFile = (ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request)) &&
                !myPlayerRequest;
        myTranscoderConfig = transcoderConfig;
        myActive = true;
    }

    protected Transcoder(TranscoderConfig transcoderConfig, Track track, WebConfig webConfig, HttpServletRequest request) {
        myTrack = track;
        myPlayerRequest = "true".equalsIgnoreCase(request.getParameter("playerRequest"));
        myTempFile = (ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request) || !webConfig.isTranscodeOnTheFlyIfPossible()) &&
                !myPlayerRequest;
        myTranscoderConfig = transcoderConfig;
        myActive = webConfig.isActiveTranscoder(transcoderConfig.getName());
    }

    public File getTranscodedFile() throws IOException {
        File cacheDir = new File(MyTunesRssUtils.getCacheDataPath() + "/transcoder/cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File file = File.createTempFile("mytunesrss_transcoded_", ".tmp", cacheDir);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            InputStream inputStream = getStream();
            IOUtils.copy(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.close();
            return file;
        } catch (IOException e) {
            file.delete();
            throw e;
        }
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

    public boolean isAvailable() {
        return myTranscoderConfig.isValidBinary();
    }

    public boolean isActive() {
        return myActive;
    }

    public InputStream getStream() throws IOException {
        return new TranscoderStream(myTranscoderConfig, getTrack());
    }

    public String getTranscoderId() {
        return myTranscoderConfig.getName();
    }

    public String getTargetContentType() {
        return myTranscoderConfig.getTargetContentType();
    }
}