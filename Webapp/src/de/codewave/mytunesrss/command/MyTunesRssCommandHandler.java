/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
    protected MyTunesRssConfig getMyTunesRssConfig() {
        return (MyTunesRssConfig)getSession().getServletContext().getAttribute(MyTunesRssConfig.class.getName());
    }

    protected boolean isAuthorized(String authHash) {
        MyTunesRssConfig config = getMyTunesRssConfig();
        return !config.isAuth() || ("" + config.getPasswordHash()).equals(authHash);
    }

    protected void authorize() {
        getSession().setAttribute("authHash", "" + getMyTunesRssConfig().getPasswordHash());
    }

    protected boolean needsAuthorization() {
        if (!getMyTunesRssConfig().isAuth() || StringUtils.isNotEmpty((String)getSession().getAttribute("authHash"))) {
            return false;
        } else {
            return true;
        }
    }

    protected void setError(String error) {
        getRequest().setAttribute("error", error);
    }

    protected void forward(MyTunesRssResource resource) throws IOException, ServletException {
        getRequest().setAttribute("servletUrl", ServletUtils.getApplicationUrl(getRequest()) + "/exec");
        getRequest().setAttribute("appUrl", ServletUtils.getApplicationUrl(getRequest()));
        forward(resource.getValue());
    }

    protected void forward(MyTunesRssCommand command) throws IOException, ServletException {
        forward("/exec/" + command.getName());
    }
}