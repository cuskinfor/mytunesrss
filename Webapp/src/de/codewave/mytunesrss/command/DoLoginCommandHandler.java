/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.commanDoLoginCommandHandlerer
 */
public class DoLoginCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        String password = getRequest().getParameter("password");
        if (password != null && needsAuthorization()) {
            if (isAuthorized(password.hashCode())) {
                WebConfig webConfig = getWebConfig();
                webConfig.setRememberLogin(Boolean.valueOf(getRequestParameter("rememberLogin", "false")));
                webConfig.save(getResponse());
                authorize();
                forward(MyTunesRssCommand.ShowPortal);
            } else {
                setError("error.login_denied");
                forward(MyTunesRssResource.Login);
            }
        } else if (needsAuthorization()) {
            forward(MyTunesRssResource.Login);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}