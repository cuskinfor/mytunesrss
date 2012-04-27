/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.bonjour.BonjourDevice;

import java.util.*;

/**
 * de.codewave.mytunesrss.remote.render.GenreRenderer
 */
public class BonjourDeviceRenderer implements Renderer<Map, BonjourDevice> {
    public Map render(BonjourDevice device) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", device.getId());
        String name = device.getName();
        int separator = name.indexOf("@");
        result.put("name", separator > -1 ? name.substring(separator + 1) : name);
        result.put("host", device.getInetAddress().getHostAddress());
        result.put("port", device.getPort());
        return result;
    }
}