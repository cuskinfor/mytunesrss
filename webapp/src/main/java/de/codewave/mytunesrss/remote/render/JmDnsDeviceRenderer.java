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
public class JmDnsDeviceRenderer implements Renderer<Map, BonjourDevice> {
    public Map render(BonjourDevice device) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", device.getId());
        result.put("name", device.getName());
        result.put("host", device.getInetAddress().getHostAddress());
        result.put("port", device.getPort());
        return result;
    }
}