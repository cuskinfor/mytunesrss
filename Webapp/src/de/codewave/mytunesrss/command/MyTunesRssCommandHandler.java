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
        return config.getPasswordHash() == authHash;
    }

    protected void authorize() {
        getSession().setAttribute("authHash", getMyTunesRssConfig().getPasswordHash());
    }

    protected int getAuthHash() {
        Integer hash = (Integer)getSession().getAttribute("authHash");
        return hash != null ? hash.intValue() : 0;
    }

    protected boolean needsAuthorization() {
        if (getSession().getAttribute("authHash") != null) {
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

    public void createSessionPagers() throws SQLException {
        if (getSession().getAttribute("artistPager") == null) {
            List<FindIndexesQuery.Index> indexes = (List<FindIndexesQuery.Index>)getDataStore().executeQuery(new FindArtistIndexesQuery());
            Pager pager = createTopPager(indexes);
            getSession().setAttribute("artistPager", pager);
        }
        if (getSession().getAttribute("albumPager") == null) {
            List<FindIndexesQuery.Index> indexes = (List<FindIndexesQuery.Index>)getDataStore().executeQuery(new FindAlbumIndexesQuery());
            Pager pager = createTopPager(indexes);
            getSession().setAttribute("albumPager", pager);
        }
    }

    private Pager createTopPager(List<FindIndexesQuery.Index> indexes) {
        List<Pager.Page> pages = new ArrayList<Pager.Page>();
        if (indexes.size() < 10) {
            for (FindIndexesQuery.Index index : indexes) {
                pages.add(new Pager.Page(index.getLetter(), index.getLetter()));
            }
        } else {
            float indexesPerPage = indexes.size() / 9;
            for (int page = 0; page < 9; page++) {
                int startIndex = (int)(page * indexesPerPage);
                int endIndex = (int)(((page + 1) * indexesPerPage) - 1);
                String value;
                if (startIndex != endIndex) {
                    value = indexes.get(startIndex).getLetter() + " - " + indexes.get(endIndex).getLetter();
                } else {
                    value = indexes.get(startIndex).getLetter();
                }
                StringBuffer key = new StringBuffer(page == 0 ? "_!" : "");
                for (int i = startIndex; i <= endIndex; i++) {
                    key.append("_").append(indexes.get(i).getLetter());
                }
                pages.add(new Pager.Page(key.substring(1), value));
            }
        }
        pages.add(new Pager.Page("", "all"));// todo: i18n word "all"
        Pager pager = new Pager(pages, pages.size());
        return pager;
    }
}