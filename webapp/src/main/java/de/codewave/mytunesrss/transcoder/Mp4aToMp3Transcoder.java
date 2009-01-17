package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class Mp4aToMp3Transcoder extends Transcoder {
    private boolean myActive;

    public Mp4aToMp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track, request, webConfig);
        myActive = webConfig.isFaad();
    }

    public boolean isAvailable() {
        return super.isAvailable() && MyTunesRss.CONFIG.isValidLameBinary() && MyTunesRss.CONFIG.isValidFaadBinary();
    }

    public boolean isActive() {
        return myActive;
    }

    public InputStream getStream() throws IOException {
        return new FaadLameTranscoderStream(getTrack(), getTargetBitrate(), getTargetSampleRate());
    }

    public String getTranscoderId() {
        return "faadlame_" + getTargetBitrate() + "_" + getTargetSampleRate();
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}