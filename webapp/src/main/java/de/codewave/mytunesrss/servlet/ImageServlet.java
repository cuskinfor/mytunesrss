/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

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
