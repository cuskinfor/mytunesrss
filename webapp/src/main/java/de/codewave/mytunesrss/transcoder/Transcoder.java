package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.transcoder.Transcoder
 */
public class Transcoder {
    private boolean myTempFile;
    private Track myTrack;
    private TranscoderConfig myTranscoderConfig;

    public static Transcoder createTranscoder(Track track, User user, String activeTranscoders, boolean tempFile) {
        TranscoderConfig transcoderConfig = user != null ? user.getForceTranscoder(track) : null;
        Transcoder transcoder = transcoderConfig != null && transcoderConfig.isValidBinary() ? new Transcoder(transcoderConfig, track, tempFile) : null;
        if (transcoder == null) {
            transcoderConfig = MyTunesRssWebUtils.getTranscoder(activeTranscoders, track);
            transcoder = transcoderConfig != null && transcoderConfig.isValidBinary() && MyTunesRssWebUtils.isActiveTranscoder(activeTranscoders, transcoderConfig.getName()) ? new Transcoder(transcoderConfig, track, tempFile) : null;
        }
        return transcoder;
    }

    protected Transcoder(TranscoderConfig transcoderConfig, Track track, boolean tempFile) {
        myTrack = track;
        myTempFile = tempFile;
        myTranscoderConfig = transcoderConfig;
    }

    public File getTranscodedFile() throws IOException {
        File file = File.createTempFile("mytunesrss_", null, new File(MyTunesRssUtils.getCacheDataPath() + "/" + MyTunesRss.CACHEDIR_TRANSCODER));
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
            FileCache.FileInfo fileInfo = MyTunesRss.STREAMING_CACHE.lock(identifier);
            File transcodedFile = fileInfo != null ? fileInfo.getFile() : null;
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