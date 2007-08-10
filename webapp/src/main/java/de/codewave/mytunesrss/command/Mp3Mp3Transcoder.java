package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;

import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class Mp3Mp3Transcoder extends Transcoder {
    private boolean myActive;

    public Mp3Mp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track.getId(), track.getFile(), request, webConfig);
        myActive = webConfig.isLame();
    }

    public boolean isAvailable() {
        return super.isAvailable() && MyTunesRss.CONFIG.isValidLameBinary();
    }

    public boolean isActive() {
        return super.isActive() || myActive;
    }

    public InputStream getStream() throws IOException {
        return new LameTranscoderStream(getFile(), MyTunesRss.CONFIG.getLameBinary(), getTargetBitrate(), getTargetSampleRate());
    }

    protected String getTranscoderId() {
        return "lame_mp3tomp3_" + getTargetBitrate() + "_" + getTargetSampleRate();
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}