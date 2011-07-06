/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShowShareLinkCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getRequest().setAttribute("text", getRequestParameter("text", null));
            if (getRequest().getParameter("rss") != null) {
                getRequest().setAttribute("rss", getRequest().getParameter("rss"));
            }
            if (getRequest().getParameter("playlist") != null) {
                getRequest().setAttribute("playlist", getRequest().getParameter("playlist"));
            }
            if (getRequest().getParameter("download") != null) {
                getRequest().setAttribute("download", getRequest().getParameter("download"));
            }
            if (getRequest().getParameter("jukebox") != null) {
                Map<String, String> jukeboxes = new LinkedHashMap<String, String>();
                for (FlashPlayerConfig flashPlayerConfig : MyTunesFunctions.flashPlayerConfigs()) {
                    jukeboxes.put(flashPlayerConfig.getName(), getRequest().getParameter("jukebox").replace("#ID#", flashPlayerConfig.getId()));
                }
                getRequest().setAttribute("jukeboxes", jukeboxes);
            }
            forward(MyTunesRssResource.ShareLink);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
