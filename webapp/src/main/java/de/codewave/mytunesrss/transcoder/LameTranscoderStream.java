package de.codewave.mytunesrss.transcoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class LameTranscoderStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(LameTranscoderStream.class);

    private Process myProcess;

    public LameTranscoderStream(Track track, String lameBinary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] command = (lameBinary + " " + MyTunesRss.CONFIG.getLameOnlyOptions()).split(" ");
        AbstractTranscoderStream.replaceTokens(command, track, outputBitRate, outputSampleRate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing command \"" + StringUtils.join(command, " ") + "\".");
        }
        myProcess = Runtime.getRuntime().exec(command);
        new Thread(new Runnable() {
            public void run() {
                try {
                    OutputStream nullOutputStream = new OutputStream() {
                        public void write(int b) throws IOException {
                            // ignore => NULL-Writer
                        }
                    };
                    IOUtils.copy(myProcess.getErrorStream(), nullOutputStream);
                    nullOutputStream.close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not dump error stream.", e);
                    }
                }
            }
        }).start();
    }

    public int read() throws IOException {
        return myProcess.getInputStream().read();
    }

    @Override
    public void close() throws IOException {
        myProcess.destroy();
        super.close();
    }
}