/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.commanDoLoginCommandHandlerer
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        String userName = getRequest().getParameter("username");
        String password = getRequest().getParameter("password");
        if (password != null && !isSessionAuthorized()) {
            byte[] passwordHash = MyTunesRss.MESSAGE_DIGEST.digest(password.getBytes("UTF-8"));
            if (isAuthorized(userName, passwordHash)) {
                WebConfig webConfig = getWebConfig();
                Boolean rememberLogin = getBooleanRequestParameter("rememberLogin", false);
                webConfig.setLoginStored(rememberLogin);
                webConfig.setUserName(userName);
                webConfig.setPasswordHash(passwordHash);
                webConfig.save(getRequest(), getResponse());
                authorizeSession(userName);
                forward(MyTunesRssCommand.ShowPortal);
            } else {
                addError(new BundleError("error.loginDenied"));
                forward(MyTunesRssResource.Login);
            }
        } else if (!isSessionAuthorized()) {
            forward(MyTunesRssResource.Login);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}