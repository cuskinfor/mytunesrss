package de.codewave.utils.servlet;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Response wrapper used with the GZIP filter.
 */
public class GzipFilterResponseWrapper extends HttpServletResponseWrapper {
    private ServletOutputStream myStream;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @throws IllegalArgumentException if the response is null
     */
    public GzipFilterResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (myStream == null) {
            setHeader("Content-Encoding", "gzip");
            myStream = new GzipFilterOutputStream(super.getOutputStream());
        }
        return myStream;
    }

    @Override
    public void setContentLength(int len) {
        // intentionally left blank
    }

    @Override
    public void setHeader(String name, String value) {
        if (!StringUtils.equals(name, "Content-Length")) {
            super.setHeader(name, value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        if (!StringUtils.equals(name, "Content-Length")) {
            super.addHeader(name, value);
        }
    }

    @Override
    public void addIntHeader(String name, int value) {
        if (!StringUtils.equals(name, "Content-Length")) {
            super.addIntHeader(name, value);
        }
    }

    @Override
    public void setIntHeader(String name, int value) {
        if (!StringUtils.equals(name, "Content-Length")) {
            super.setIntHeader(name, value);
        }
    }
}
