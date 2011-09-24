package de.codewave.utils.servlet;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * Servlet output stream used with the GZIP filter.
 */
public class GzipFilterOutputStream extends ServletOutputStream {
    private static final Logger LOGGER = LoggerFactory.getLogger(GzipFilterOutputStream.class);

    private GzipCompressorOutputStream myZipOutputStream;

    public GzipFilterOutputStream(ServletOutputStream delegate) throws IOException {
        LOGGER.debug("Creating GZIP output stream.");
        myZipOutputStream = new GzipCompressorOutputStream(delegate);
    }

    public void write(int b) throws IOException {
        myZipOutputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        myZipOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        myZipOutputStream.flush();
    }
}