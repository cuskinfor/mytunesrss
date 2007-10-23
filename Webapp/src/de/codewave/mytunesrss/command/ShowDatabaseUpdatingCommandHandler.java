/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.ShowDatabaseUpdatingCommandHandler
 */
public class ShowDatabaseUpdatingCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        forward(MyTunesRssResource.DatabaseUpdating);
    }
}