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
        getRequest().setAttribute("error", error);
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
        getRequest().setAttribute("pagerInitialPage", "a_b_c");
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
        List<Pager.PagerItem> pagerItems = null;
        if (getSession().getAttribute("albumPager") == null) {
            pagerItems = createAlphabetPagerItems();
            for (Pager.PagerItem item : pagerItems) {
                int albumCount = getAlbumCount(item.getKey().replace("_", "% ") + "%");
                item.getUserData().put("active", albumCount > 0);
            }
            getSession().setAttribute("albumPager", new Pager(pagerItems, pagerItems.size()));
        }
        // artist pager
        if (getSession().getAttribute("artistPager") == null) {
            pagerItems = createAlphabetPagerItems();
            for (Pager.PagerItem item : pagerItems) {
                int artistCount = getArtistCount(item.getKey().replace("_", "% ") + "%");
                item.getUserData().put("active", artistCount > 0);
            }
            getSession().setAttribute("artistPager", new Pager(pagerItems, pagerItems.size()));
        }
    }

    protected Pager createPager(int itemCount, int current) {
        int pageSize = getWebConfig().getPageSize();
        if (pageSize > 0) {
            List<Pager.PagerItem> pagerItems = new ArrayList<Pager.PagerItem>();
            int page = 0;
            for (int index = 0; index < itemCount; index += pageSize) {
                pagerItems.add(new Pager.PagerItem(Integer.toString(page), Integer.toString(page + 1)));
                page++;
            }
            Pager pager = new Pager(pagerItems, 10);
            pager.moveTo(current);
            return pager;
        }
        return null;
    }

    private List<Pager.PagerItem> createAlphabetPagerItems() {
        List<Pager.PagerItem> pagerItems = new ArrayList<Pager.PagerItem>();
        pagerItems.add(new Pager.PagerItem("0_1_2_3_4_5_6_7_8_9", "0 - 9"));
        pagerItems.add(new Pager.PagerItem("a_b_c", "A - C"));
        pagerItems.add(new Pager.PagerItem("d_e_f", "D - F"));
        pagerItems.add(new Pager.PagerItem("g_h_i", "G - I"));
        pagerItems.add(new Pager.PagerItem("j_k_l", "J - L"));
        pagerItems.add(new Pager.PagerItem("m_n_o", "M - O"));
        pagerItems.add(new Pager.PagerItem("p_q_r_s", "P - S"));
        pagerItems.add(new Pager.PagerItem("t_u_v", "T - V"));
        pagerItems.add(new Pager.PagerItem("w_x_y_z", "W - Z"));
        pagerItems.add(new Pager.PagerItem("", "all"));
        return pagerItems;
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