package de.codewave.mytunesrss.command;

import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import java.io.*;
import java.text.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class LameTranscoderStream extends InputStream {
    private static final Log LOG = LogFactory.getLog(LameTranscoderStream.class);
    private static String LAME_ARGUMENTS = "--quiet -b {bitrate} --resample {samplerate} {infile} -";

    static {

    }

    protected static String getLameSampleRate(int outputSampleRate) {
        StringBuffer lameRate = new StringBuffer();
        lameRate.append(Integer.toString(outputSampleRate / 1000)).append(".");
        lameRate.append(StringUtils.stripEnd(StringUtils.leftPad(Integer.toString(outputSampleRate % 1000), 3, "0"), "0"));
        return lameRate.toString();
    }

    private Process myProcess;

    public LameTranscoderStream(File file, String lameBinary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] command = (lameBinary + " " + LAME_ARGUMENTS).split(" ");
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