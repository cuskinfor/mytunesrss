/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

public class OpenSearchCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void execute() throws Exception {
        forward(MyTunesRssResource.OpenSearch);
    }
}
