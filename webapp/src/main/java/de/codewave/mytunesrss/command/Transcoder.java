package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.io.*;
import org.apache.commons.io.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.Transcoder
 */
public class Transcoder {
    private File myFile;
    private boolean myLame;
    private int myLameTargetBitrate;
    private int myLameTargetSampleRate;

    public Transcoder(File file, WebConfig webConfig, HttpServletRequest request) {
        myFile = file;
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

    public synchronized File getTranscodedFile() throws IOException {
        File file = FileCache.getFile(myFile.getAbsolutePath());
        if (file == null) {
            file = File.createTempFile("mytunesrss-", ".tmp");
            file.deleteOnExit();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            InputStream inputStream = getStream();
            IOUtils.copy(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.close();
            FileCache.add(myFile.getAbsolutePath(), file, 600000); // expiration time is 10 minutes
        }
        return file;
    }
}