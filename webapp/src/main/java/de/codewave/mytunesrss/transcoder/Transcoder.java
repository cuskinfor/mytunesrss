package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.command.StatusCodeSender;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.servlet.StreamSender;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.transcoder.Transcoder
 */
public class Transcoder {
    private Track myTrack;
    private TranscoderConfig myTranscoderConfig;

    public static Transcoder createTranscoder(Track track, User user, String activeTranscoders) {
        TranscoderConfig transcoderConfig = user != null ? user.getForceTranscoder(track) : null;
        Transcoder transcoder = transcoderConfig != null ? new Transcoder(transcoderConfig, track) : null;
        if (transcoder == null) {
            transcoderConfig = MyTunesRssUtils.getTranscoder(activeTranscoders, track);
            transcoder = transcoderConfig != null && MyTunesRssUtils.isActiveTranscoder(activeTranscoders, transcoderConfig.getName()) ? new Transcoder(transcoderConfig, track) : null;
        }
        return transcoder;
    }

    protected Transcoder(TranscoderConfig transcoderConfig, Track track) {
        myTranscoderConfig = transcoderConfig;
        myTrack = track;
    }

    public StreamSender getStreamSender(File originalFile, long ifModifiedSince) throws IOException {
        if (ifModifiedSince != -1 && !isModifiedSince(originalFile, ifModifiedSince)) {
            // not modified
            return new StatusCodeSender(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            return new StreamSender(getStream(originalFile, ifModifiedSince), getTargetContentType(), 0);
        }
    }

    private boolean isModifiedSince(File originalFile, long ts) {
        File cacheFile = getCacheFile();
        return !cacheFile.isFile() || (originalFile.isFile() && originalFile.lastModified() > cacheFile.lastModified()) || (cacheFile.lastModified() / 1000 > ts / 1000);
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

    public String getTargetContentType() {
        return myTranscoderConfig.getTargetContentType();
    }
}
