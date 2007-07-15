/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.io.*;
import org.apache.commons.logging.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.net.*;

/**
 * Servlet for delivering style sheets depending on the currently chosen theme.
 */
public class ImageServlet extends ThemeServlet {
    private static final Log LOG = LogFactory.getLog(ImageServlet.class);

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        File image = getFile(httpServletRequest, "/images");
        httpServletResponse.setContentType(URLConnection.guessContentTypeFromName(image.getName()));
        httpServletResponse.setContentLength((int)image.length());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending image \"" + image.getAbsolutePath() + "\" with content type \"" + httpServletResponse.getContentType() + "\".");
        }
        IOUtils.copy(new FileInputStream(image), httpServletResponse.getOutputStream());
    }
}
