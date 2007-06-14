package de.codewave.mytunesrss.servlet;

import org.apache.commons.io.*;
import org.apache.commons.logging.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

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
