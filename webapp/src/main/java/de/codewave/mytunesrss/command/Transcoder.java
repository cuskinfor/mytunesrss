package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class Transcoder {
    private String myTrackId;
    private File myFile;
    private boolean myLame;
    private int myLameTargetBitrate;
    private int myLameTargetSampleRate;

    public static Transcoder createTranscoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        Transcoder transcoder = new Transcoder(track, webConfig, request);
        return transcoder.isTranscoder() ? transcoder : null;
    }

    public Transcoder(Track track, WebConfig webConfig, HttpServletRequest request) {
        myTrackId = track.getId();
        myFile = track.getFile();
        init(webConfig, request);
    }

    private void init(WebConfig webConfig, HttpServletRequest request) {
        if (myFile.getName().toLowerCase().endsWith(".mp3")) {
            myLame = webConfig.isLame();
            myLameTargetBitrate = webConfig.getLameTargetBitrate();
            myLameTargetSampleRate = webConfig.getLameTargetSampleRate();
            if (StringUtils.isNotEmpty(request.getParameter("lame"))) {
                String[] splitted = request.getParameter("lame").split(",");
                if (splitted.length == 2) {
                    myLame = true;
                    myLameTargetBitrate = Integer.parseInt(splitted[0]);
                    myLameTargetSampleRate = Integer.parseInt(splitted[1]);
                }
            }
        }
    }

    public boolean isTranscoder() {
        return MyTunesRss.REGISTRATION.isRegistered() && myLame && myLameTargetBitrate > 0 && myLameTargetSampleRate > 0 &&
                MyTunesRss.CONFIG.isValidLameBinary();
    }

    public InputStream getStream() throws IOException {
        return new LameTranscoderStream(myFile, MyTunesRss.CONFIG.getLameBinary(), myLameTargetBitrate, myLameTargetSampleRate);
    }

    public File getTranscodedFile() throws IOException {
        File cacheDir = new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/transcoder/cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File file = File.createTempFile("mytunesrss_", ".tmp", cacheDir);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        InputStream inputStream = getStream();
        IOUtils.copy(inputStream, fileOutputStream);
        inputStream.close();
        fileOutputStream.close();
        return file;
    }

    protected String getTranscoderId() {
        return "lame_mp3tomp3_" + myLameTargetBitrate + "_" + myLameTargetSampleRate;
    }
}