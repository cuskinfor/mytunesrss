package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.ShowResourceCommandHandler
 */
public class ShowResourceCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            forward(MyTunesRssResource.valueOf(getRequestParameter("resource", MyTunesRssResource.Login.name())));
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}