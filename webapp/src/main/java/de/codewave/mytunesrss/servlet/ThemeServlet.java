/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;

/**
 * de.codewave.mytunesrss.servlet.ThemeServlet
 */
public class ThemeServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ThemeServlet.class);

    protected File getFile(HttpServletRequest httpServletRequest) {
        String resourcePath = httpServletRequest.getPathInfo().substring(httpServletRequest.getPathInfo().indexOf("/", 1));
        String theme = httpServletRequest.getPathInfo().substring(2, httpServletRequest.getPathInfo().indexOf("/", 2));
        if (LOG.isDebugEnabled()) {
            LOG.debug("File \"" + resourcePath + "\" for theme \"" + StringUtils.defaultIfEmpty(theme, "BUILT-IN DEFAULT") + "\" requested.");
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

    protected void doGetStyle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        File stylesheet = getFile(httpServletRequest);
        long ifModifiedSince = httpServletRequest.getDateHeader("If-Modified-Since");
        if (ifModifiedSince > -1 && stylesheet.lastModified() / 1000 <= ifModifiedSince / 1000) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            httpServletResponse.setDateHeader("Last-Modified", stylesheet.lastModified());
            httpServletResponse.setHeader("Cache-Control", MyTunesRssWebUtils.createCacheControlValue(0));
            httpServletResponse.setContentType("text/css");
            httpServletResponse.setContentLength((int) stylesheet.length());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending stylesheet \"" + stylesheet.getAbsolutePath() + "\".");
            }
            try (Reader inReader = new InputStreamReader(new FileInputStream(stylesheet), Charset.forName("UTF-8"))) {
                IOUtils.copy(inReader, httpServletResponse.getWriter());
            }
        }
    }

    protected void doGetImage(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        File image = getFile(httpServletRequest);
        long ifModifiedSince = httpServletRequest.getDateHeader("If-Modified-Since");
        if (ifModifiedSince > -1 && image.lastModified() / 1000 <= ifModifiedSince / 1000) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            httpServletResponse.setDateHeader("Last-Modified", image.lastModified());
            httpServletResponse.setHeader("Cache-Control", MyTunesRssWebUtils.createCacheControlValue(0));
            httpServletResponse.setContentType(MyTunesRssUtils.guessContentType(image));
            httpServletResponse.setContentLength((int) image.length());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sending image \"" + image.getAbsolutePath() + "\" with content type \"" + httpServletResponse.getContentType() + "\".");
            }
            try (FileInputStream inStream = new FileInputStream(image)) {
                IOUtils.copy(inStream, httpServletResponse.getOutputStream());
            } catch (IOException ignored) {
                // ignore exception
            }
        }
    }
}
