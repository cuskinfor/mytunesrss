package de.codewave.mytunesrss.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Servlet for delivering style sheets depending on the currently chosen theme.
 */
public class StylesServlet extends ThemeServlet {
    private static final Log LOG = LogFactory.getLog(StylesServlet.class);

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        File stylesheet = getFile(httpServletRequest, "/styles");
        httpServletResponse.setContentType("text/css");
        httpServletResponse.setContentLength((int)stylesheet.length());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending stylesheet \"" + stylesheet.getAbsolutePath() + "\".");
        }
        IOUtils.copy(new FileReader(stylesheet), httpServletResponse.getWriter());
    }
}
