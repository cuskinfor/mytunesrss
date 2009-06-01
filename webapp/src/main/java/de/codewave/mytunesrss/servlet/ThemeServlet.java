/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.PrefsUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.ThemeServlet
 */
public abstract class ThemeServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(ThemeServlet.class);

    protected File getFile(HttpServletRequest httpServletRequest, String resourceBasePath) {
        String theme = MyTunesRssWebUtils.getWebConfig(httpServletRequest).getTheme();
        if (LOG.isDebugEnabled()) {
            LOG.debug("File \"" + httpServletRequest.getPathInfo() + "\" for theme \"" + theme + "\" requested.");
        }
        try {
            if (StringUtils.isNotEmpty(theme)) {
                File file = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/themes/" + theme + resourceBasePath +
                        httpServletRequest.getPathInfo());
                if (file.exists()) {
                    // addon theme found
                    return file;
                }
                file = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/themes/" + theme + resourceBasePath + httpServletRequest.getPathInfo());
                if (file.exists()) {
                    // built-in theme found
                    return file;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not find file \"" + httpServletRequest.getPathInfo() + "\" for theme \"" + theme +
                            "\". Using default resource.");
                }
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find file \"" + httpServletRequest.getPathInfo() + "\" for theme \"" + theme + "\". Using default resource.", e);
            }
        }
        return new File(getServletContext().getRealPath(resourceBasePath) + httpServletRequest.getPathInfo());
    }
}
