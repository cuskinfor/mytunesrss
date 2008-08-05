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
public class Mp3ToMp3Transcoder extends Transcoder {
    private boolean myActive;

    public Mp3ToMp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track, request, webConfig);
        myActive = webConfig.isLame();
    }

    public boolean isAvailable() {
        return super.isAvailable() && MyTunesRss.CONFIG.isValidLameBinary();
    }

    public boolean isActive() {
        return super.isActive() || myActive;
    }

    public InputStream getStream() throws IOException {
        return new LameTranscoderStream(getTrack(), MyTunesRss.CONFIG.getLameBinary(), getTargetBitrate(), getTargetSampleRate());
    }

    public String getTranscoderId() {
        return "lame_" + getTargetBitrate() + "_" + getTargetSampleRate();
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}