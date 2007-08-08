package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.*;
import de.codewave.utils.servlet.*;
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
    private boolean myTempFile;

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
            myTempFile = ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request) || !webConfig.isLameOnTheFlyIfPossible();
            if (StringUtils.isNotEmpty(request.getParameter("lame"))) {
                String[] splitted = request.getParameter("lame").split(",");
                if (splitted.length == 3) {
                    myLame = true;
                    myLameTargetBitrate = Integer.parseInt(splitted[0]);
                    myLameTargetSampleRate = Integer.parseInt(splitted[1]);
                    myTempFile |= !Boolean.parseBoolean(splitted[2]);
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

    public StreamSender getStreamSender() throws IOException {
        final String identifier = myTrackId + "_" + getTranscoderId();
        if (myTempFile) {
            File transcodedFile = MyTunesRss.STREAMING_CACHE.lock(identifier);
            if (transcodedFile == null) {
                transcodedFile = getTranscodedFile();
                MyTunesRss.STREAMING_CACHE.add(identifier, transcodedFile, MyTunesRss.CONFIG.getStreamingCacheTimeout() * 60000);
                MyTunesRss.STREAMING_CACHE.lock(identifier);
            }
            return new FileSender(transcodedFile, getTargetContentType(), (int)transcodedFile.length()) {
                protected void afterSend() {
                    MyTunesRss.STREAMING_CACHE.unlock(identifier);
                }
            };
        } else {
            return new StreamSender(getStream(), getTargetContentType(), 0);
        }
    }

    protected String getTranscoderId() {
        return "lame_mp3tomp3_" + myLameTargetBitrate + "_" + myLameTargetSampleRate;
    }

    public String getTargetContentType() {
        return "audio/mp3";
    }
}