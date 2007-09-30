package de.codewave.mytunesrss.command;

import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public abstract class AbstractTranscoderStream extends InputStream {
    private static final Log LOG = LogFactory.getLog(AbstractTranscoderStream.class);

    private Process myTargetProcess;
    private Process mySourceProcess;

    public AbstractTranscoderStream(File file, String targetBinary, String sourceBinary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] targetCommand = (targetBinary + " " + getTargetArguments()).split(" ");
        for (int i = 0; i < targetCommand.length; i++) {
            if ("{bitrate}".equals(targetCommand[i])) {
                targetCommand[i] = Integer.toString(outputBitRate);
            } else if ("{samplerate}".equals(targetCommand[i])) {
                targetCommand[i] = Integer.toString(outputSampleRate);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getTargetName() + " command \"" + StringUtils.join(targetCommand, " ") + "\".");
        }
        String[] sourceCommand = (sourceBinary + " " + getSourceArguments()).split(" ");
        for (int i = 0; i < sourceCommand.length; i++) {
            if ("{infile}".equals(sourceCommand[i])) {
                sourceCommand[i] = file.getAbsolutePath();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getSourceName() + " command \"" + StringUtils.join(sourceCommand, " ") + "\".");
        }
        mySourceProcess = Runtime.getRuntime().exec(sourceCommand);
        myTargetProcess = Runtime.getRuntime().exec(targetCommand);
        new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(mySourceProcess.getInputStream(), myTargetProcess.getOutputStream());
                    myTargetProcess.getOutputStream().close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not copy " + getSourceName() + " output to " + getTargetName() + " input stream.", e);
                    }
                }
            }
        }).start();
    }

    public int read() throws IOException {
        return myTargetProcess.getInputStream().read();
    }

    @Override
    public void close() throws IOException {
        mySourceProcess.destroy();
        myTargetProcess.destroy();
        super.close();
    }

    protected abstract String getSourceName();

    protected abstract String getTargetName();

    protected abstract String getSourceArguments();

    protected abstract String getTargetArguments();
}