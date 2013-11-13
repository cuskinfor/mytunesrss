/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.config.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * de.codewave.mytunesrss.servlet.ThemeServlet
 */
public class FlashPlayerServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(FlashPlayerServlet.class);

    protected File getFile(String resourcePath) {
        File file = new File(MyTunesRss.PREFERENCES_DATA_PATH + resourcePath);
        if (file.exists()) {
            return file;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Could not find user flash player file \"" + resourcePath + "\". Using default resource.");
        }
        return new File(getServletContext().getRealPath(resourcePath));
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String[] pathInfo = StringUtils.split(httpServletRequest.getPathInfo(), "/");
        if (pathInfo.length == 1) {
            String resourcePath = "/flashplayer/" + pathInfo[0] + "/index.html";
            if (LOG.isDebugEnabled()) {
                LOG.debug("Flash player file \"" + resourcePath + "\" requested.");
            }
            httpServletResponse.setContentType("text/html");
            InputStream htmlInputStream = new FileInputStream(getFile(resourcePath));
            try {
                String html = IOUtils.toString(htmlInputStream);
                httpServletResponse.getWriter().println(html.replace("{PLAYLIST_URL}", httpServletRequest.getParameter("url")));
            } finally {
                htmlInputStream.close();
            }
        } else {
            String resourcePath = httpServletRequest.getRequestURI().substring(StringUtils.trimToEmpty(httpServletRequest.getContextPath()).length());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Flash player file \"" + httpServletRequest.getPathInfo() + "\" requested.");
            }
            File file = getFile(resourcePath);
            String contentType = URLConnection.guessContentTypeFromName(file.getName());
            if (StringUtils.isBlank(contentType) && StringUtils.endsWithIgnoreCase(file.getName(), ".swf")) {
                contentType = "application/x-shockwave-flash"; // special handling
            }
            httpServletResponse.setContentType(contentType);
            int length = (int) file.length();
            httpServletResponse.setContentLength(length);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending flash player file \"" + file.getAbsolutePath() + "\" with content-type \"" + contentType + "\" and length \"" + length + "\".");
            }
            FileInputStream inStream = new FileInputStream(file);
            try {
                IOUtils.copy(inStream, httpServletResponse.getOutputStream());
            } finally {
                inStream.close();
            }
        }
    }

}
