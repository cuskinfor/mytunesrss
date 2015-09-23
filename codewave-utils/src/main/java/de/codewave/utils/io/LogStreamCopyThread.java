package de.codewave.utils.io;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.util.UUID;

public class LogStreamCopyThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogStreamCopyThread.class);

    /**
     * Log level for commons logging.
     */
    public enum LogLevel {
        Debug(), Info(), Warn(), Error()
    }

    private InputStream myInputStream;
    private boolean myCloseInput;
    private Logger myLogger;
    private LogLevel myLogLevel;
    private String myPrelude;
    private String myCoda;

    /**
     * Create a new log stream copy thread.
     *
     * @param inputStream The input stream.
     * @param closeInput  <code>true</code> to close the input stream automatically or <code>false</code> to leave it open.
     * @param logger      The log to write the stream into.
     * @param level       The log level to use.
     * @param prelude     The first text to log (may be NULL or empty)
     * @param coda        The last text to log (may be NULL or empty)
     */
    public LogStreamCopyThread(InputStream inputStream, boolean closeInput, Logger logger, LogLevel level, String prelude, String coda) {
        myInputStream = inputStream;
        myCloseInput = closeInput;
        myLogger = logger;
        myLogLevel = level;
        myPrelude = prelude;
        myCoda = coda;
    }

    @Override
    public void run() {
        MDC.put("id", UUID.randomUUID().toString());
        if (StringUtils.isNotBlank(myPrelude)) {
            log(myPrelude);
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(myInputStream));
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                log(line);
            }
            if (myCloseInput) {
                myInputStream.close();
            }
        } catch (Throwable e) {
            LOGGER.debug("LogStreamCopyThread exited with error.", e);
        } finally {
            if (StringUtils.isNotBlank(myCoda)) {
                log(myCoda);
            }
            MDC.remove("id");
        }
    }

    private void log(String line) {
        switch (myLogLevel) {
            case Debug:
                myLogger.debug(line);
                break;
            case Info:
                myLogger.info(line);
                break;
            case Warn:
                myLogger.warn(line);
                break;
            case Error:
                myLogger.error(line);
                break;
        }
    }
}
