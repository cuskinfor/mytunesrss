/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.ShowUploadCommandHandler
 */
public class ShowUploadCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getSession().setAttribute("uploadPercentage", 0);
            getRequest().setAttribute("datasources", MyTunesRss.CONFIG.getUploadableDatasources());
            forward(MyTunesRssResource.ShowUpload);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
