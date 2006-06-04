/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.commanDoLoginCommandHandlerer
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        String password = getRequest().getParameter("password");
        if (password != null && needsAuthorization()) {
            byte[] authHash = MyTunesRss.MESSAGE_DIGEST.digest(password.getBytes("UTF-8"));
            if (isAuthorized(authHash)) {
                WebConfig webConfig = getWebConfig();
                Boolean rememberLogin = Boolean.valueOf(getRequestParameter("rememberLogin", "false"));
                webConfig.setPasswordHashStored(rememberLogin);
                webConfig.setPasswordHash(authHash);
                webConfig.save(getResponse());
                authorize();
                forward(MyTunesRssCommand.ShowPortal);
            } else {
                addError(new BundleError("error.loginDenied"));
                forward(MyTunesRssResource.Login);
            }
        } else if (needsAuthorization()) {
            forward(MyTunesRssResource.Login);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}