package de.codewave.mytunesrss.command;

import org.apache.commons.logging.*;
import org.apache.commons.lang.*;
import org.apache.commons.io.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public class Faad2LameTranscoderStream extends InputStream {
    private static final Log LOG = LogFactory.getLog(Faad2LameTranscoderStream.class);
    private static String LAME_ARGUMENTS = "--quiet -b {bitrate} --resample {samplerate} - -";
    private static String FAAD2_ARGUMENTS = "-f 2 -w {infile}";

    protected static String getLameSampleRate(int outputSampleRate) {
        StringBuffer lameRate = new StringBuffer();
        lameRate.append(Integer.toString(outputSampleRate / 1000)).append(".");
        lameRate.append(StringUtils.stripEnd(StringUtils.leftPad(Integer.toString(outputSampleRate % 1000), 3, "0"), "0"));
        return lameRate.toString();
    }

    private Process myLameProcess;
    private Process myFaad2Process;

    public Faad2LameTranscoderStream(File file, String lameBinary, String faad2Binary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] lameCommand = (lameBinary + " " + LAME_ARGUMENTS).split(" ");
        for (int i = 0; i < lameCommand.length; i++) {
            if ("{bitrate}".equals(lameCommand[i])) {
                lameCommand[i] = Integer.toString(outputBitRate);
            } else if ("{samplerate}".equals(lameCommand[i])) {
                lameCommand[i] = Integer.toString(outputSampleRate);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing lame command \"" + StringUtils.join(lameCommand, " ") + "\".");
        }
        String[] faad2Command = (faad2Binary + " " + FAAD2_ARGUMENTS).split(" ");
        for (int i = 0; i < faad2Command.length; i++) {
            if ("{infile}".equals(faad2Command[i])) {
                faad2Command[i] = file.getAbsolutePath();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing faad2 command \"" + StringUtils.join(faad2Command, " ") + "\".");
        }
        myFaad2Process = Runtime.getRuntime().exec(faad2Command);
        myLameProcess = Runtime.getRuntime().exec(lameCommand);
        new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(myFaad2Process.getInputStream(), myLameProcess.getOutputStream());
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not copy faad2 output to lame input stream.", e);
                    }
                }
            }
        }).start();
    }

    public int read() throws IOException {
        return myLameProcess.getInputStream().read();
    }

    @Override
    public void close() throws IOException {
        myLameProcess.destroy();
        myFaad2Process.destroy();
        super.close();
    }
}