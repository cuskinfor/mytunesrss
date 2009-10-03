package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.ShowIphoneStartupCommandHandler
 */
public class ShowIphoneStartupCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void execute() throws Exception {
        forward(MyTunesRssResource.IphoneStartup);
    }
}