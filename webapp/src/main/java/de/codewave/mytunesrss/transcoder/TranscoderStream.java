package de.codewave.mytunesrss.transcoder;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.FindTrackImageQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.meta.Image;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.io.StreamCopyThread;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.transcoder.TranscoderStream
 */
public class TranscoderStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(TranscoderStream.class);

    private Process myProcess;
    private TranscoderConfig myTranscoderConfig;
    private InputStream myInputStream;

    TranscoderStream(TranscoderConfig transcoderConfig, File inputFile) throws IOException {
        myTranscoderConfig = transcoderConfig;
        final String[] transcoderCommand = new String[] {
                MyTunesRss.CONFIG.getEffectiveVlcExecutable().getAbsolutePath(),
                inputFile.getAbsolutePath(),
                "vlc://quit",
                "--intf=telnet",
                "--sout-transcode-audio-sync",
                "--sout=#transcode{" + transcoderConfig.getOptions() + "}:std{access=file,mux=" + StringUtils.defaultIfBlank(transcoderConfig.getTargetMux(), "dummy") + ",dst=-}"
        };
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getName() + " command \"" + StringUtils.join(transcoderCommand, " ") + "\".");
        }
        myProcess = new ProcessBuilder(transcoderCommand).start();
        myInputStream = myProcess.getInputStream();
        new LogStreamCopyThread(myProcess.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug).start();
    }

    public int read() throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.read();
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.read(bytes);
    }

    @Override
    public int read(byte[] bytes, int start, int length) throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.read(bytes, start, length);
    }

    @Override
    public long skip(long l) throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.skip(l);
    }

    @Override
    public int available() throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.available();
    }

    @Override
    public void mark(int i) {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        myInputStream.mark(i);
    }

    @Override
    public void reset() throws IOException {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        myInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        if (myInputStream == null) {
            throw new IllegalStateException("No input stream available.");
        }
        return myInputStream.markSupported();
    }

    @Override
    public void close() throws IOException {
        myInputStream = null;
        if (myProcess != null) {
            myProcess.destroy();
        }
    }

    protected String getName() {
        return myTranscoderConfig.getName();
    }
}