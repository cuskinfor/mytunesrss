/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import javax.servlet.ServletException;
import java.io.IOException;

public class ClearTwitterAuthCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws IOException, ServletException {
        if (isSessionAuthorized()) {
            getAuthUser().clearTwitterAuth();
            redirect(getRequest().getParameter("backUrl"));
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
