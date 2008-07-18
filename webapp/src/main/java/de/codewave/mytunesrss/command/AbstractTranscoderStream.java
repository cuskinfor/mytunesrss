package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FileSuffixInfo;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.io.StreamCopyThread;
import de.codewave.utils.io.LogStreamCopyThread;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStream
 */
public abstract class AbstractTranscoderStream extends InputStream {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTranscoderStream.class);
    private static final Properties INTERNAL_PROPERTIES = new Properties();
    private static final Properties USER_PROPERTIES = new Properties();

    static {
        try {
            INTERNAL_PROPERTIES.load(FileSuffixInfo.class.getResourceAsStream("transcoder.properties"));
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load internal transcoder properties.", e);
            }
        }
        try {
            File file = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/transcoder.properties");
            if (file.isFile()) {
                USER_PROPERTIES.load(new FileInputStream(file));
            }
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not load user transcoder properties: " + e.getMessage());
            }
        }
    }

    private Process myTargetProcess;
    private Process mySourceProcess;

    public AbstractTranscoderStream(File file, String targetBinary, String sourceBinary, int outputBitRate, int outputSampleRate) throws IOException {
        String[] targetCommand = new String[getTargetArguments().split(" ").length + 1];
        targetCommand[0] = targetBinary;
        int i = 1;
        for (String part : getTargetArguments().split(" ")) {
            targetCommand[i++] = part;
        }
        for (i = 0; i < targetCommand.length; i++) {
            if ("{bitrate}".equals(targetCommand[i])) {
                targetCommand[i] = Integer.toString(outputBitRate);
            } else if ("{samplerate}".equals(targetCommand[i])) {
                targetCommand[i] = Integer.toString(outputSampleRate);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getTargetName() + " command \"" + StringUtils.join(targetCommand, " ") + "\".");
        }
        String[] sourceCommand = new String[getSourceArguments().split(" ").length + 1];
        sourceCommand[0] = sourceBinary;
        i = 1;
        for (String part : getSourceArguments().split(" ")) {
            sourceCommand[i++] = part;
        }
        for (i = 0; i < sourceCommand.length; i++) {
            if ("{infile}".equals(sourceCommand[i])) {
                sourceCommand[i] = file.getAbsolutePath();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("executing " + getSourceName() + " command \"" + StringUtils.join(sourceCommand, " ") + "\".");
        }
        mySourceProcess = Runtime.getRuntime().exec(sourceCommand);
        myTargetProcess = Runtime.getRuntime().exec(targetCommand);
        new StreamCopyThread(mySourceProcess.getInputStream(), false, myTargetProcess.getOutputStream(), true).start();
        new LogStreamCopyThread(mySourceProcess.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug).start();
        new LogStreamCopyThread(myTargetProcess.getErrorStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug).start();
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

    private String getSourceArguments() {
        return getPropertyValue(getSourceName() + "." + getTargetName() + ".source");
    }

    static String getPropertyValue(String key) {
        return USER_PROPERTIES.getProperty(key, INTERNAL_PROPERTIES.getProperty(key, ""));
    }

    private String getTargetArguments() {
        return getPropertyValue(getSourceName() + "." + getTargetName() + ".target");
    }
}