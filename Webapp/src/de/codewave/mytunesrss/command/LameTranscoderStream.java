package de.codewave.mytunesrss.command;

import org.apache.commons.logging.*;
import org.apache.commons.io.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class LameTranscoderStream extends InputStream {
    private static final Log LOG = LogFactory.getLog(LameTranscoderStream.class);

    private Process myProcess;

    public LameTranscoderStream(File file, String lameBinary, int outputBitRate) throws IOException {
        String[] command = new String[] {lameBinary, "-b " + outputBitRate, "--cbr", file.getAbsolutePath(), "-"};
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing command \"" + command + "\".");
        }
        myProcess = Runtime.getRuntime().exec(command);
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