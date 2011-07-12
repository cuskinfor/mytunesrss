/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import java.util.LinkedHashMap;
import java.util.Map;

public class ShowShareLinkCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getRequest().setAttribute("text", getRequestParameter("text", ""));
            MyTunesRssCommandCallBuilder builder = new MyTunesRssCommandCallBuilder(MyTunesRssCommand.ShowShareLink).addParam("text", getRequestParameter("text", ""));
            if (getRequest().getParameter("rss") != null) {
                builder.addParam("rss", getRequest().getParameter("rss"));
                getRequest().setAttribute("rss", getRequest().getParameter("rss"));
            }
            if (getRequest().getParameter("playlist") != null) {
                builder.addParam("playlist", getRequest().getParameter("playlist"));
                getRequest().setAttribute("playlist", getRequest().getParameter("playlist"));
            }
            if (getRequest().getParameter("download") != null) {
                builder.addParam("download", getRequest().getParameter("download"));
                getRequest().setAttribute("download", getRequest().getParameter("download"));
            }
            if (getRequest().getParameter("jukebox") != null) {
                builder.addParam("jukebox", getRequest().getParameter("jukebox"));
                Map<String, String> jukeboxes = new LinkedHashMap<String, String>();
                for (FlashPlayerConfig flashPlayerConfig : MyTunesFunctions.flashPlayerConfigs()) {
                    jukeboxes.put(flashPlayerConfig.getName(), getRequest().getParameter("jukebox").replace("#ID#", flashPlayerConfig.getId()));
                }
                getRequest().setAttribute("jukeboxes", jukeboxes);
            }
            getRequest().setAttribute("selfLink", builder.getCall(getRequest()));
            forward(MyTunesRssResource.ShareLink);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
