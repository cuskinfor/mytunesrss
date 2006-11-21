/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.ShowUploadCommandHandler
 */
public class ShowUploadCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        forward(MyTunesRssResource.ShowUpload);
    }
}