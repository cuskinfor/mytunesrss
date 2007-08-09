package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class Mp3Mp3Transcoder extends Transcoder {
    private boolean myLame;
    private int myLameTargetBitrate;
    private int myLameTargetSampleRate;

    public Mp3Mp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track.getId(), track.getFile(), request, webConfig);
        setTempFileRequested(init(webConfig, request));
    }

    private boolean init(WebConfig webConfig, HttpServletRequest request) {
        boolean tempFile = true;
        myLame = webConfig.isLame();
        myLameTargetBitrate = webConfig.getLameTargetBitrate();
        myLameTargetSampleRate = webConfig.getLameTargetSampleRate();
        if (StringUtils.isNotEmpty(request.getParameter("lame"))) {
            String[] splitted = request.getParameter("lame").split(",");
            if (splitted.length == 3) {
                myLame = true;
                myLameTargetBitrate = Integer.parseInt(splitted[0]);
                myLameTargetSampleRate = Integer.parseInt(splitted[1]);
                tempFile |= !Boolean.parseBoolean(splitted[2]);
            }
        }
        return tempFile;
    }

    public boolean isTranscoder() {
        return MyTunesRss.REGISTRATION.isRegistered() && myLame && myLameTargetBitrate > 0 && myLameTargetSampleRate > 0 &&
                MyTunesRss.CONFIG.isValidLameBinary();
    }

    public InputStream getStream() throws IOException {
        return new LameTranscoderStream(getFile(), MyTunesRss.CONFIG.getLameBinary(), myLameTargetBitrate, myLameTargetSampleRate);
    }

    protected String getTranscoderId() {
        return "lame_mp3tomp3_" + myLameTargetBitrate + "_" + myLameTargetSampleRate;
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}