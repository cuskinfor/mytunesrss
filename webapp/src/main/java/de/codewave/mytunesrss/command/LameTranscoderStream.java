package de.codewave.mytunesrss.command;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.codewave.mytunesrss.MyTunesRss;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class LameTranscoderStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(LameTranscoderStream.class);

    private Process myProcess;

    public LameTranscoderStream(File file, String lameBinary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] command = (lameBinary + " " + MyTunesRss.CONFIG.getMp3OnlyOptions()).split(" ");
        for (int i = 0; i < command.length; i++) {
            if ("{bitrate}".equals(command[i])) {
                command[i] = Integer.toString(outputBitRate);
            } else if ("{samplerate}".equals(command[i])) {
                command[i] = Integer.toString(outputSampleRate);
            } else if ("{infile}".equals(command[i])) {
                command[i] = file.getAbsolutePath();
            }
        }
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