package de.codewave.mytunesrss.remote;

import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.JsonpFilterResponseWrapper
 */
public class JsonpFilterResponseWrapper extends HttpServletResponseWrapper {
    private ServletOutputStream myStream;
    private String myJsonp;

    /**
     * Constructs a response adaptor wrapping the given response.
     *
     * @throws IllegalArgumentException if the response is null
     */
    public JsonpFilterResponseWrapper(HttpServletResponse response, String jsonp) {
        super(response);
        myJsonp = jsonp;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (myStream == null) {
            setHeader("Content-Type", "text/javascript");
            myStream = new JsonpFilterOutputStream(super.getOutputStream(), super.getCharacterEncoding(), myJsonp + "(", ")");
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