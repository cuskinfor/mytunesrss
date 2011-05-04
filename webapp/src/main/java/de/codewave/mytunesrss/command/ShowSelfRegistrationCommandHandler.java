package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

public class ShowSelfRegistrationCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void execute() throws Exception {
        forward(MyTunesRssResource.SelfRegistration);
    }
}
