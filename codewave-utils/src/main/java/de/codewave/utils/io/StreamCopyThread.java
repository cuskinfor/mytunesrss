package de.codewave.utils.io;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Thread which copies data from an input stream into an output stream until the input
 * stream's end is reached. The streams are optionally closed afterwards. This class depends
 * on the commons-io library.
 */
public class StreamCopyThread extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamCopyThread.class);

    private InputStream myInputStream;
    private OutputStream myOutputStream;
    private boolean myCloseInput;
    private boolean myCloseOutput;

    public StreamCopyThread(InputStream inputStream, boolean closeInput, OutputStream outputStream, boolean closeOutput) {
        super("codewave-utils:stream-copy-thread");
        myInputStream = inputStream;
        myCloseInput = closeInput;
        myOutputStream = outputStream;
        myCloseOutput = closeOutput;
    }

    public boolean isCloseInput() {
        return myCloseInput;
    }

    public void setCloseInput(boolean closeInput) {
        myCloseInput = closeInput;
    }

    public boolean isCloseOutput() {
        return myCloseOutput;
    }

    public void setCloseOutput(boolean closeOutput) {
        myCloseOutput = closeOutput;
    }

    @Override
    public void run() {
        try {
            Exception raised = null;
            try {
                org.apache.commons.io.IOUtils.copy(myInputStream, myOutputStream);
                myOutputStream.close();
            } catch (IOException e) {
                raised = e;
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not copy stream.", e);
                }
            } finally {
                if (myCloseInput) {
                    try {
                        myInputStream.close();
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not close input stream.", e);
                        }
                    } finally {
                        if (myCloseOutput) {
                            try {
                                myOutputStream.close();
                            } catch (IOException e) {
                                if (LOGGER.isErrorEnabled()) {
                                    LOGGER.error("Could not close output stream.", e);
                                }
                            }
                        }
                    }
                }
            }
            afterExecution(raised);
        } catch (Throwable e) {
            LOGGER.error("StreamCopyThread exited with error.", e);
        }
    }

    OutputStream getOutputStream() {
        return myOutputStream;
    }

    /**
     * Hook method which is called after the stream has been copied completely. If an
     * exception is raised during the copy process it is passed into this method. Otherwise
     * the exception parameter will be <code>null</code>. This method does nothing in the
     * default implementation and is meant to be overridden in subclasses.
     *
     * @param e Exception which has been raised during the copy process or <code>null</code> if
     * no exception has been raised.
     */
    protected void afterExecution(Exception e) {
        // intentionally left blank
    }
}