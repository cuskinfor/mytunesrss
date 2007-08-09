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
public class M4aMp3Transcoder extends Transcoder {
    private boolean myFaad2;
    private int myFaad2TargetBitrate;
    private int myFaad2TargetSampleRate;

    public M4aMp3Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        super(track.getId(), track.getFile(), request, webConfig);
        setTempFileRequested(init(webConfig, request));
    }

    private boolean init(WebConfig webConfig, HttpServletRequest request) {
        boolean tempFile = true;
        myFaad2 = webConfig.isFaad2();
        myFaad2TargetBitrate = webConfig.getFaad2TargetBitrate();
        myFaad2TargetSampleRate = webConfig.getFaad2TargetSampleRate();
        if (StringUtils.isNotEmpty(request.getParameter("faad2"))) {
            String[] splitted = request.getParameter("faad2").split(",");
            if (splitted.length == 3) {
                myFaad2 = true;
                myFaad2TargetBitrate = Integer.parseInt(splitted[0]);
                myFaad2TargetSampleRate = Integer.parseInt(splitted[1]);
                tempFile = !Boolean.parseBoolean(splitted[2]);
            }
        }
        return tempFile;
    }

    public boolean isTranscoder() {
        return MyTunesRss.REGISTRATION.isRegistered() && myFaad2 && myFaad2TargetBitrate > 0 && myFaad2TargetSampleRate > 0 &&
                MyTunesRss.CONFIG.isValidLameBinary();
    }

    public InputStream getStream() throws IOException {
        return new Faad2LameTranscoderStream(getFile(),
                                             MyTunesRss.CONFIG.getLameBinary(),
                                             MyTunesRss.CONFIG.getFaad2Binary(),
                                             myFaad2TargetBitrate,
                                             myFaad2TargetSampleRate);
    }

    protected String getTranscoderId() {
        return "faad2lame_m4atomp3_" + myFaad2TargetBitrate + "_" + myFaad2TargetSampleRate;
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}