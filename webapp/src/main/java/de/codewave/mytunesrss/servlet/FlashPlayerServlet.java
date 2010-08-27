/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLConnection;

/**
 * de.codewave.mytunesrss.servlet.ThemeServlet
 */
public class FlashPlayerServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(FlashPlayerServlet.class);

    protected File getFile(HttpServletRequest httpServletRequest) {
        String resourcePath = httpServletRequest.getRequestURI().substring(StringUtils.trimToEmpty(httpServletRequest.getContextPath()).length());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Flash player file \"" + httpServletRequest.getPathInfo() + "\" requested.");
        }
        try {
            File file = new File(MyTunesRssUtils.getPreferencesDataPath() + "/flashplayer/" + resourcePath);
            if (file.exists()) {
                return file;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find user flash player file \"" + resourcePath + "\". Using default resource.");
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find user flash player file \"" + resourcePath + "\". Using default resource.");
            }
        }
        return new File(getServletContext().getRealPath(resourcePath));
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        File file = getFile(httpServletRequest);
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        httpServletResponse.setContentType(contentType);
        int length = (int) file.length();
        httpServletResponse.setContentLength(length);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending flash player file \"" + file.getAbsolutePath() + "\" with content-type \"" + contentType + "\" and length \"" + length + "\".");
        }
        IOUtils.copy(new FileReader(file), httpServletResponse.getWriter());
    }
}
