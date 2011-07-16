/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

public class CloseWindowCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void execute() throws Exception {
        forward(MyTunesRssResource.CloseWindow);
    }
}
