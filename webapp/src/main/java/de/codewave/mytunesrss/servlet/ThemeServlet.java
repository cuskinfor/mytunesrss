/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.PrefsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileInputStream;
import java.net.URLConnection;

/**
 * de.codewave.mytunesrss.servlet.ThemeServlet
 */
public class ThemeServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ThemeServlet.class);

    protected File getFile(HttpServletRequest httpServletRequest) {
        String resourcePath = httpServletRequest.getRequestURI().substring(StringUtils.trimToEmpty(httpServletRequest.getContextPath()).length());
        String theme = StringUtils.defaultIfEmpty(MyTunesRssWebUtils.getWebConfig(httpServletRequest).getTheme(), MyTunesRss.CONFIG.getDefaultUserInterfaceTheme());
        if (LOG.isDebugEnabled()) {
            LOG.debug("File \"" + httpServletRequest.getPathInfo() + "\" for theme \"" + StringUtils.defaultIfEmpty(theme, "BUILT-IN DEFAULT") + "\" requested.");
        }
        if (StringUtils.isNotEmpty(theme)) {
            File file = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/themes/" + theme + resourcePath);
            if (file.exists()) {
                // addon theme found
                return file;
            }
            file = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/themes/" + theme + resourcePath);
            if (file.exists()) {
                // built-in theme found
                return file;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find file \"" + resourcePath + "\" for theme \"" + theme +
                        "\". Using default resource.");
            }
        }
        return new File(getServletContext().getRealPath(resourcePath));
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        if (StringUtils.endsWithIgnoreCase(httpServletRequest.getRequestURI(), ".css")) {
            doGetStyle(httpServletRequest, httpServletResponse);
        } else {
            doGetImage(httpServletRequest, httpServletResponse);
        }
    }

    protected void doGetStyle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        File stylesheet = getFile(httpServletRequest);
        httpServletResponse.setContentType("text/css");
        httpServletResponse.setContentLength((int) stylesheet.length());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending stylesheet \"" + stylesheet.getAbsolutePath() + "\".");
        }
        IOUtils.copy(new FileReader(stylesheet), httpServletResponse.getWriter());
    }

    protected void doGetImage(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        File image = getFile(httpServletRequest);
        httpServletResponse.setContentType(URLConnection.guessContentTypeFromName(image.getName()));
        httpServletResponse.setContentLength((int) image.length());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending image \"" + image.getAbsolutePath() + "\" with content type \"" + httpServletResponse.getContentType() + "\".");
        }
        IOUtils.copy(new FileInputStream(image), httpServletResponse.getOutputStream());
    }
}
