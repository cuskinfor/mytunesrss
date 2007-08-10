package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;

import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class M4aMp3Transcoder extends Transcoder {
    private boolean myActive;

    public M4aMp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track.getId(), track.getFile(), request, webConfig);
        myActive = webConfig.isFaad2();
    }

    public boolean isAvailable() {
        return super.isAvailable() && MyTunesRss.CONFIG.isValidLameBinary() && MyTunesRss.CONFIG.isValidFaad2Binary();
    }

    public boolean isActive() {
        return super.isActive() || myActive;
    }

    public InputStream getStream() throws IOException {
        return new Faad2LameTranscoderStream(getFile(),
                                             MyTunesRss.CONFIG.getLameBinary(),
                                             MyTunesRss.CONFIG.getFaad2Binary(), getTargetBitrate(), getTargetSampleRate());
    }

    protected String getTranscoderId() {
        return "faad2lame_m4atomp3_" + getTargetBitrate() + "_" + getTargetSampleRate();
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}