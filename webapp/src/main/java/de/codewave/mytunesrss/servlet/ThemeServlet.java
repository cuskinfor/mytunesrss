/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.utils.PrefsUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.ThemeServlet
 */
public abstract class ThemeServlet extends HttpServlet {
    private static final Log LOG = LogFactory.getLog(ThemeServlet.class);

    protected File getFile(HttpServletRequest httpServletRequest, String resourceBasePath) {
        String theme = MyTunesRssWebUtils.getWebConfig(httpServletRequest).getTheme();
        if (LOG.isDebugEnabled()) {
            LOG.debug("File \"" + httpServletRequest.getPathInfo() + "\" for theme \"" + theme + "\" requested.");
        }
        try {
            if (StringUtils.isNotEmpty(theme)) {
                File file = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/themes/" + theme + resourceBasePath + httpServletRequest.getPathInfo());
                if (file.exists()) {
                    return file;
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not find file \"" + httpServletRequest.getPathInfo() + "\" for theme \"" + theme +
                                "\". Using default resource.");
                    }
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
