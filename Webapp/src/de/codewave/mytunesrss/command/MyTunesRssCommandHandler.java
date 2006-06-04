/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.utils.servlet.*;
import de.codewave.utils.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.lang.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
    protected MyTunesRssConfig getMyTunesRssConfig() {
        return (MyTunesRssConfig)getSession().getServletContext().getAttribute(MyTunesRssConfig.class.getName());
    }

    protected boolean isAuthorized(byte[] authHash) {
        MyTunesRssConfig config = getMyTunesRssConfig();
        return Arrays.equals(config.getPasswordHash(), authHash);
    }

    protected void authorize() {
        getSession().setAttribute("authHash", MiscUtils.toHexString(getMyTunesRssConfig().getPasswordHash()));
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
                    byte[] requestAuthHash = MiscUtils.fromHexString(getRequestParameter("authHash", ""));
                    if (isAuthorized(requestAuthHash)) {
                        authorize();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // intentionally left blank
                }
            }
            return true;
        }
    }

    protected void addError(Error error) {
        List<Error> errors = (List<Error>)getSession().getAttribute("errors");
        if (errors == null) {
            synchronized(getSession()) {
                errors = (List<Error>)getSession().getAttribute("errors");
                if (errors == null) {
                    errors = new ArrayList<Error>();
                    getSession().setAttribute("errors", errors);
                }
            }
        }
        errors.add(error);
    }

    protected DataStore getDataStore() {
        return (DataStore)getContext().getAttribute(DataStore.class.getName());
    }

    protected void forward(MyTunesRssResource resource) throws IOException, ServletException {
        prepareRequestForResource();
        resource.beforeForward(getRequest(), getResponse());
        getResponse().setHeader("Cache-Control", "no-cache");
        getResponse().setHeader("Pragma", "no-cache");
        getResponse().setDateHeader("Expires", 0);
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

    protected void redirect(String url) throws IOException {
        getResponse().sendRedirect(url.replace("&amp;", "&"));
    }

    protected Map<String, Boolean> getStates() {
        Map<String, Boolean> states = (Map<String, Boolean>)getSession().getAttribute("states");
        if (states == null) {
            synchronized(getSession()) {
                states = (Map<String, Boolean>)getSession().getAttribute("states");
                if (states == null) {
                    states = new HashMap<String, Boolean>();
                    getSession().setAttribute("states", states);
                }
            }
        }
        return states;
    }

    public void execute() throws Exception {
        try {
            if (needsAuthorization() && getWebConfig().isPasswordHashStored() && isAuthorized(getWebConfig().getPasswordHash())) {
                authorize();
                executeAuthorized();
            } else if (needsAuthorization()) {
                forward(MyTunesRssResource.Login);
            } else {
                executeAuthorized();
            }
        } catch (Exception e) {
            getSession().removeAttribute("errors");
            redirect(ServletUtils.getApplicationUrl(getRequest()) + "/mytunesrss" + "/" + MyTunesRssCommand.ShowFatalError.getName());
        }
    }

    public void executeAuthorized() throws Exception {
        // intentionally left blank
    }

    protected Pager createPager(int itemCount, int current) {
        int pageSize = getWebConfig().getEffectivePageSize();
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