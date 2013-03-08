package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.servlet.FileSender;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

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
        Transcoder transcoder = transcoderConfig != null ? new Transcoder(transcoderConfig, track, tempFile) : null;
        if (transcoder == null) {
            transcoderConfig = MyTunesRssWebUtils.getTranscoder(activeTranscoders, track);
            transcoder = transcoderConfig != null && MyTunesRssWebUtils.isActiveTranscoder(activeTranscoders, transcoderConfig.getName()) ? new Transcoder(transcoderConfig, track, tempFile) : null;
        }
        return transcoder;
    }

    protected Transcoder(TranscoderConfig transcoderConfig, Track track, boolean tempFile) {
        myTranscoderConfig = transcoderConfig;
        myTrack = track;
        myTempFile = tempFile;
    }

    public StreamSender getStreamSender(File originalFile) throws IOException {
        if (myTempFile) {
            IOUtils.copyLarge(getStream(originalFile), new NullOutputStream());
        }
        return new StreamSender(getStream(originalFile), getTargetContentType(), 0);
    }

    public InputStream getStream(File originalFile) throws IOException {
        return new TranscoderStream(myTranscoderConfig, originalFile, getCacheFile());
    }

    private File getCacheFile() {
        return MyTunesRss.STREAMING_CACHE.getFileForName(StringUtils.replaceChars(myTranscoderConfig.getName(), ' ', '_') + "_" + myTrack.getId());
    }

    public String getTranscoderId() {
        return myTranscoderConfig.getName();
    }

    public String getTargetContentType() {
        return myTranscoderConfig.getTargetContentType();
    }
}