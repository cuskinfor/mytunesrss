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

public class AddSmartPlaylistCriteriaCommandHandler extends SaveSmartPlaylistCommandHandler {

    @Override
    public void executeAuthorized() throws IOException, ServletException {
        Map<String,Object> redisplayModel = createRedisplayModel(null);
        Map<String, String> newModel = new HashMap<String, String>();
        String fieldType = getRequestParameter("newFieldType", "");
        if (fieldType.endsWith(".not")) {
            newModel.put("fieldType", fieldType.substring(0, fieldType.length() - ".not".length()));
            newModel.put("invert", "true");
        } else {
            newModel.put("fieldType", fieldType);
            newModel.put("invert", "true");
        }
        newModel.put("pattern", "");
        ((List <Map<String, String>>)redisplayModel.get("smartInfos")).add(newModel);
        getRequest().setAttribute("smartPlaylist", redisplayModel);
        forward(MyTunesRssResource.EditSmartPlaylist);
    }
}
