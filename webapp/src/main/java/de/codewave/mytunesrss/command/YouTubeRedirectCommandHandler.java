package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.external.YouTubeLoader;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.YouTubeRedirectCommandHandler
 */
public class YouTubeRedirectCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        redirect(MyTunesRssUtils.getYouTubeUrl(getRequestParameter("track", null)));
    }

}
