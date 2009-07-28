package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

/**
 * de.codewave.mytunesrss.command.ShowIphoneIndexCommandHandler
 */
public class ShowIphoneIndexCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void execute() throws Exception {
        forward(MyTunesRssResource.IphoneIndex);
    }
}