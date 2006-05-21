/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
    private static final Log LOG = LogFactory.getLog(MyTunesRssCommandHandler.class);

    protected MyTunesRssConfig getMyTunesRssConfig() {
        return (MyTunesRssConfig)getSession().getServletContext().getAttribute(MyTunesRssConfig.class.getName());
    }

    protected boolean isAuthorized(int authHash) {
        MyTunesRssConfig config = getMyTunesRssConfig();
        return !config.isAuth() || config.getPasswordHash() == authHash;
    }

    protected void authorize() {
        getSession().setAttribute("authHash", getMyTunesRssConfig().getPasswordHash());
    }

    protected int getAuthHash() {
        Integer hash = (Integer)getSession().getAttribute("authHash");
        return hash != null ? hash.intValue() : 0;
    }

    protected boolean needsAuthorization() {
        if (!getMyTunesRssConfig().isAuth() || getSession().getAttribute("authHash") != null) {
            return false;
        } else {
            if (StringUtils.isNotEmpty(getRequest().getParameter("authHash"))) {
                try {
                    int requestAuthHash = Integer.parseInt(getRequest().getParameter("authHash"));
                    return !isAuthorized(requestAuthHash);
                } catch (NumberFormatException e) {
                    // intentionally left blank
                }
            }
            return true;
        }
    }

    protected void setError(String error) {
        getSession().setAttribute("error", error);
    }

    protected DataStore getDataStore() {
        return (DataStore)getContext().getAttribute(DataStore.class.getName());
    }

    protected void forward(MyTunesRssResource resource) throws IOException, ServletException {
        prepareRequestForResource();
        forward(resource.getValue());
    }

    private void prepareRequestForResource() {
        getRequest().setAttribute("servletUrl", ServletUtils.getApplicationUrl(getRequest()) + "/mytunesrss");
        getRequest().setAttribute("appUrl", ServletUtils.getApplicationUrl(getRequest()));
        getWebConfig(); // result not needed, method also fills the request attribute "config"
    }

    protected WebConfig getWebConfig() {
        WebConfig webConfig = (WebConfig)getRequest().getAttribute("config");
        if (webConfig == null) {
            webConfig = new WebConfig();
            webConfig.load(getRequest());
            getRequest().setAttribute("config", webConfig);
        }
        return webConfig;
    }

    protected void forward(MyTunesRssCommand command) throws IOException, ServletException {
        prepareRequestForResource();
        forward("/mytunesrss/" + command.getName());
    }

    public void execute() throws Exception {
        createSessionPagers();
        if (needsAuthorization() && getWebConfig().isRememberLogin()) {
            authorize();
            executeAuthorized();
        } else if (needsAuthorization()) {
            forward(MyTunesRssResource.Login);
        } else {
            executeAuthorized();
        }
    }

    public void executeAuthorized() throws Exception {
        // intentionally left blank
    }

    protected String[] getNonEmptyParameterValues(String name) {
        String[] values = getRequest().getParameterValues(name);
        if (values != null && values.length > 0) {
            List<String> nonEmptyValues = new ArrayList<String>();
            for (String value : values) {
                if (StringUtils.isNotEmpty(value)) {
                    nonEmptyValues.add(value);
                }
            }
            return nonEmptyValues.toArray(new String[nonEmptyValues.size()]);
        }
        return null;
    }

    public void createSessionPagers() {
        int pageSize = getWebConfig().getPageSize();
        // album pager
        List<Pager.Page> pages = null;
        if (getSession().getAttribute("albumPager") == null) {
            pages = createAlphabetPagerItems();
            String initialPager = null;
            for (Pager.Page item : pages) {
                int albumCount = getAlbumCount(item.getKey().replace("_", "% ") + "%");
                boolean active = albumCount > 0;
                if (active && initialPager == null && !"0_1_2_3_4_5_6_7_8_9".equals(item.getKey())) {
                    initialPager = item.getKey();
                }
                item.getUserData().put("active", active);
            }
            getSession().setAttribute("albumPager", new Pager(pages, pages.size()));
            getSession().setAttribute("albumInitialPager", initialPager);
        }
        // artist pager
        if (getSession().getAttribute("artistPager") == null) {
            pages = createAlphabetPagerItems();
            String initialPager = null;
            for (Pager.Page item : pages) {
                int artistCount = getArtistCount(item.getKey().replace("_", "% ") + "%");
                boolean active = artistCount > 0;
                if (active && initialPager == null && !"0_1_2_3_4_5_6_7_8_9".equals(item.getKey())) {
                    initialPager = item.getKey();
                }
                item.getUserData().put("active", active);
            }
            getSession().setAttribute("artistPager", new Pager(pages, pages.size()));
            getSession().setAttribute("artistInitialPager", initialPager);
        }
    }

    protected Pager createPager(int itemCount, int current) {
        int pageSize = getWebConfig().getPageSize();
        if (pageSize > 0) {
            List<Pager.Page> pages = new ArrayList<Pager.Page>();
            int page = 0;
            for (int index = 0; index < itemCount; index += pageSize) {
                pages.add(new Pager.Page(Integer.toString(page), Integer.toString(page + 1)));
                page++;
            }
            Pager pager = new Pager(pages, 10);
            pager.moveToPage(current);
            return pager;
        }
        return null;
    }

    private List<Pager.Page> createAlphabetPagerItems() {
        List<Pager.Page> pages = new ArrayList<Pager.Page>();
        pages.add(new Pager.Page("0_1_2_3_4_5_6_7_8_9", "0 - 9"));
        pages.add(new Pager.Page("a_b_c", "A - C"));
        pages.add(new Pager.Page("d_e_f", "D - F"));
        pages.add(new Pager.Page("g_h_i", "G - I"));
        pages.add(new Pager.Page("j_k_l", "J - L"));
        pages.add(new Pager.Page("m_n_o", "M - O"));
        pages.add(new Pager.Page("p_q_r_s", "P - S"));
        pages.add(new Pager.Page("t_u_v", "T - V"));
        pages.add(new Pager.Page("w_x_y_z", "W - Z"));
        pages.add(new Pager.Page("", "all"));
        return pages;
    }

    private int getAlbumCount(String startPatterns) {
        try {
            return getCount(getDataStore().executeQuery(new FindAlbumQuery(StringUtils.split(startPatterns, " "))));
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.warn("Could not get album count.", e);
            }
        }
        return 0;
    }

    private int getArtistCount(String startPatterns) {
        try {
            return getCount(getDataStore().executeQuery(new FindArtistQuery(StringUtils.split(startPatterns, " "))));
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.warn("Could not get album count.", e);
            }
        }
        return 0;
    }

    private int getCount(Collection result) {
        return result != null ? result.size() : 0;
    }

}