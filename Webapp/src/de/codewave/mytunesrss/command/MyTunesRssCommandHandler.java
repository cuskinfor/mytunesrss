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

import javax.servlet.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
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
        List<Pager.Page> pages = (List<Pager.Page>)getDataStore().executeQuery(new FindPagesStatement());
        if (pages != null) {
            getRequest().setAttribute("albumPager", new Pager(pages, pages.size()));
            getRequest().setAttribute("artistPager", new Pager(pages, pages.size()));
            getRequest().setAttribute("albumInitialPager", pages.get(0).getKey());
            getRequest().setAttribute("artistInitialPager", pages.get(0).getKey());
        }

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
}