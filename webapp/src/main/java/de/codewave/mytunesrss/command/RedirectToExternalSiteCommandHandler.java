package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;

/**
 * de.codewave.mytunesrss.command.RedirectToExternalSiteCommandHandler
 */
public class RedirectToExternalSiteCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        String sitename = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("site"));
        String album = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("album"));
        String artist = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("artist"));
        String title = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("title"));
        getResponse().sendRedirect(MyTunesRss.CONFIG.getExternalSiteUrl(sitename, album, artist, title));
    }
}