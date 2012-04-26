/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.datastore.statement.Genre;
import de.codewave.mytunesrss.jmdns.JmDnsDevice;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * de.codewave.mytunesrss.remote.render.GenreRenderer
 */
public class JmDnsDeviceRenderer implements Renderer<Map, JmDnsDevice> {
    public Map render(JmDnsDevice device) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", device.getId());
        result.put("name", device.getName());
        result.put("host", device.getInetAddress().getHostAddress());
        result.put("port", device.getPort());
        return result;
    }
}