package de.codewave.utils.servlet;

import de.codewave.utils.io.IOUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * de.codewave.utils.servlet.FileSender
 */
public class StreamSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamSender.class);

    private InputStream myStream;
    private long myContentLength;
    private String myContentType;
    private StreamSender.ByteSentCounter myCounter = new ByteSentCounter() {
        public void add(int count) {
            // dummy which does nothing
        }

        public void notifyBegin() {
            // dummy which does nothing
        }

        public void notifyEnd() {
            // dummy which does nothing
        }
    };
    private OutputStreamWrapper myWrapper;
    private StreamListener myStreamListener;

    public StreamSender(InputStream stream, String contentType, long contentLength) {
        myStream = stream;
        myContentType = contentType;
        myContentLength = contentLength;
    }

    public StreamListener getStreamListener() {
        return myStreamListener;
    }

    public void setStreamListener(StreamListener streamListener) {
        myStreamListener = streamListener;
    }

    public void setCounter(StreamSender.ByteSentCounter counter) {
        myCounter = counter;
    }

    public void sendHeadResponse(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(myContentType);
        if (myContentLength > 0) {
            response.setHeader("Content-Length", Long.toString(myContentLength));
        }
        afterSend();
        org.apache.commons.io.IOUtils.closeQuietly(myStream);
    }

    public void setOutputStreamWrapper(OutputStreamWrapper wrapper) {
        myWrapper = wrapper;
    }

    public void sendGetResponse(HttpServletRequest request, HttpServletResponse response, boolean throwExceptions) throws IOException {
        try {
            RangeHeader rangeHeader = new RangeHeader(request);
            if (rangeHeader.isRangeRequest()) {
                LOGGER.debug("Received range request with 'Range' header \"" + request.getHeader("Range") + "\".");
                if (myContentLength <= 0) {
                    File tempFile = File.createTempFile("codewave-utils-", ".tmp");
                    tempFile.deleteOnExit();
                    OutputStream outputStream = new FileOutputStream(tempFile);
                    try {
                        org.apache.commons.io.IOUtils.copy(myStream, outputStream);
                        org.apache.commons.io.IOUtils.closeQuietly(myStream);
                        myStream = new FileInputStream(tempFile);
                        myContentLength = tempFile.length();
                    } finally {
                        if (outputStream != null) {
                            org.apache.commons.io.IOUtils.closeQuietly(outputStream);
                        }
                    }
                }
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                rangeHeader.setResponseHeaders(response, myContentLength);
            } else {
                LOGGER.debug("Received default request.");
                response.setStatus(HttpServletResponse.SC_OK);
            }
            LOGGER.debug("Setting content type \"" + myContentType + "\".");
            response.setContentType(myContentType);
            long sendSize = rangeHeader.getSize(myContentLength);
            if (sendSize > 0) {
                LOGGER.debug("Setting content length \"" + sendSize + "\".");
                response.setHeader("Content-Length", Long.toString(sendSize));
            }

            if (sendSize != 0 || !rangeHeader.isRangeRequest()) {
                InputStream inputStream = null;
                BufferedOutputStream outputStream = null;
                try {
                    inputStream = new BufferedInputStream(myStream);
                    OutputStream os = new CloseShieldOutputStream(response.getOutputStream());
                    outputStream = new BufferedOutputStream(myWrapper != null ? myWrapper.wrapStream(os) : os);
                    long sendFirstByte = rangeHeader.getFirstByte(myContentLength);
                    if (sendFirstByte > 0) {
                        //noinspection StatementWithEmptyBody
                        for (long skipped = inputStream.skip(sendFirstByte); skipped < sendFirstByte; skipped += inputStream.skip(sendFirstByte - skipped));
                    }
                    int totalByteSent = 0;
                    int data = 0;
                    myCounter.notifyBegin();
                    while (data != -1 && (totalByteSent < sendSize || sendSize == 0)) {
                        data = inputStream.read();
                        if (data > -1) {
                            outputStream.write(data);
                            totalByteSent++;
                            if (totalByteSent % 10000 == 0) {
                                if (myCounter != null) {
                                    myCounter.add(10000);
                                }
                                response.flushBuffer();
                            }
                        }
                    }
                    if (myCounter != null) {
                        myCounter.add(totalByteSent % 10000);
                    }
                    outputStream.flush();
                    response.flushBuffer();
                    LOGGER.debug("Successfully sent " + totalByteSent + " bytes.");
                } catch (IOException e) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Problem while sending data.", e);
                    } else {
                        LOGGER.debug("Problem while sending data (" + e.getClass().getName() + "): \"" + e.getMessage() + "\".");
                    }
                    if (throwExceptions) {
                        throw e;
                    }
                } finally {
                    IOUtils.close(inputStream);
                    IOUtils.close(outputStream);
                    myCounter.notifyEnd();
                }
            }
        } finally {
            afterSend();
            org.apache.commons.io.IOUtils.closeQuietly(myStream);
        }
    }

    protected void setStream(InputStream stream) {
        myStream = stream;
    }

    public InputStream getStream() {
        return myStream;
    }

    protected void afterSend() {
        if (myStreamListener != null) {
            myStreamListener.afterSend();
        }
    }

    public static interface ByteSentCounter {
        void add(int count);
        void notifyBegin();
        void notifyEnd();
    }

    public static interface OutputStreamWrapper {
        OutputStream wrapStream(OutputStream stream);
    }
}
