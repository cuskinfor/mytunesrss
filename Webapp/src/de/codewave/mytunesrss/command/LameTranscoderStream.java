package de.codewave.mytunesrss.command;

import org.apache.commons.logging.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class LameTranscoderStream extends InputStream {
    private static final Log LOG = LogFactory.getLog(LameTranscoderStream.class);

    protected static String getLameSampleRate(int outputSampleRate) {
        StringBuffer lameRate = new StringBuffer();
        lameRate.append(Integer.toString(outputSampleRate / 1000)).append(".");
        lameRate.append(StringUtils.stripEnd(StringUtils.leftPad(Integer.toString(outputSampleRate % 1000), 3, "0"), "0"));
        return lameRate.toString();
    }

    private Process myProcess;

    public LameTranscoderStream(File file, String lameBinary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] command = new String[] {lameBinary, "-b", Integer.toString(outputBitRate), "--resample", getLameSampleRate(outputSampleRate), file.getAbsolutePath(), "-"};
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing command \"" + StringUtils.join(command, " ") + "\".");
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