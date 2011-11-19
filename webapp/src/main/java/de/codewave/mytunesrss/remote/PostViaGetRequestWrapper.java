package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * de.codewave.mytunesrss.remote.PostViaGetRequestWrapper
 */
public class PostViaGetRequestWrapper extends HttpServletRequestWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostViaGetRequestWrapper.class);

    private String myBody;
    private ServletInputStream myStream;
    private BufferedReader myReader;

    public PostViaGetRequestWrapper(HttpServletRequest request, String body) {
        super(request);
        myBody = body;
        LOGGER.debug("Using body \"" + body + "\" for post-via-get request wrapper.");
        try {
            final InputStream stream = new ByteArrayInputStream(MiscUtils.getUtf8Bytes(myBody));
            myStream = new ServletInputStream() {
                public int read() throws IOException {
                    return stream.read();
                }
            };
            myReader = new BufferedReader(new InputStreamReader(myStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not create POST body.", e);
        }
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return myReader;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return myStream;
    }
}