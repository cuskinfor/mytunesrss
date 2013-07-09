/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DelSmartPlaylistCriteriaCommandHandler extends SaveSmartPlaylistCommandHandler {

    @Override
    public void executeAuthorized() throws IOException, ServletException {
        getRequest().setAttribute("smartPlaylist", createRedisplayModel(getRequestParameter("remove", null), null));
        forward(MyTunesRssResource.EditSmartPlaylist);
    }
}
