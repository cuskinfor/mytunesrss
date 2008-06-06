package de.codewave.mytunesrss.remote.render;

import de.codewave.mytunesrss.network.RemoteServer;

import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.remote.render.RemoteServerRenderer
 */
public class RemoteServerRenderer implements Renderer<Map<String, Object>, RemoteServer> {
    public Map<String, Object> render(RemoteServer o) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("address", o.getAddress());
        result.put("name", o.getName());
        result.put("port", o.getPort());
        return result;
    }
}