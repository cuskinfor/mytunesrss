package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.StatusCodeSender;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

    public StreamSender getStreamSender(File originalFile, long ifModifiedSince) throws IOException {
        InputStream stream = getStream(originalFile, ifModifiedSince);
        if (ifModifiedSince != -1 && stream == null) {
            // not modified
            return new StatusCodeSender(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            if (myTempFile) {
                IOUtils.copyLarge(stream, new NullOutputStream());
                stream = getStream(originalFile, ifModifiedSince);
            }
            return new StreamSender(stream, getTargetContentType(), 0);
        }
    }

    public InputStream getStream(File originalFile, long ifModifiedSince) throws IOException {
        File cacheFile = getCacheFile();
        if (originalFile.isFile() && cacheFile.isFile() && originalFile.lastModified() > cacheFile.lastModified()) {
            // cached file is outdated
            MyTunesRss.TRANSCODER_CACHE.deleteByName(cacheFile.getName());
        }
        if (cacheFile.isFile() && ifModifiedSince != -1 && (cacheFile.lastModified() / 1000) <= (ifModifiedSince / 1000))  {
            return null; // cache file has not been modified since specified timestamp
        }
        return new TranscoderStream(myTranscoderConfig, originalFile, cacheFile);
    }

    private File getCacheFile() {
        return new File(MyTunesRss.TRANSCODER_CACHE.getBaseDir(), myTranscoderConfig.getCacheFilePrefix() + "_" + myTrack.getId());
    }

    public String getTranscoderId() {
        return myTranscoderConfig.getName();
    }

    public String getTargetContentType() {
        return myTranscoderConfig.getTargetContentType();
    }
}
