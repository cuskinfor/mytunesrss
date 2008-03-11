/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.ShowFatalErrorCommandHandler
 */
public class ShowFatalErrorCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void execute() throws Exception {
        forward(MyTunesRssResource.FatalError);
    }
}