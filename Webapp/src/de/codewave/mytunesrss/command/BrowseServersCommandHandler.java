/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.network.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseServersCommandHandler
 */
public class BrowseServersCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void execute() throws Exception {
        List<RemoteServer> servers = (List<RemoteServer>)getSession().getAttribute("remoteServers");
        if (servers == null) {
            servers = MulticastService.getOtherInstances();
            getSession().setAttribute("remoteServers", servers);
        }
        if (servers != null && servers.size() > 0) {
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && servers.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(servers.size(), current);
                getRequest().setAttribute("indexPager", pager);
                servers = servers.subList(current * pageSize, Math.min((current * pageSize) + pageSize, servers.size()));
            }
            getRequest().setAttribute("servers", servers);
            forward(MyTunesRssResource.BrowseServers);
        } else {
            addError(new BundleError("error.noRemoteServers"));
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}