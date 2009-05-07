package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.WebConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class AudioTranscoder extends Transcoder {
    private boolean myActive;
    private TranscoderConfig myTranscoderConfig;

    public AudioTranscoder(TranscoderConfig transcoderConfig, Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track, request, webConfig);
        myTranscoderConfig = transcoderConfig;
        myActive = webConfig.isActiveTranscoder(transcoderConfig.getName());
    }

    public boolean isAvailable() {
        return super.isAvailable() && MyTunesRss.CONFIG.isValidLameBinary() && myTranscoderConfig.isValidBinary();
    }

    public boolean isActive() {
        return myActive;
    }

    public InputStream getStream() throws IOException {
        return new AudioTranscoderStream(myTranscoderConfig, getTrack(), getTargetBitrate(), getTargetSampleRate());
    }

    public String getTranscoderId() {
        return myTranscoderConfig.getName() + "_" + getTargetBitrate() + "_" + getTargetSampleRate();
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}