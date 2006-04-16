/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.catalina.startup.*;
import org.apache.catalina.*;
import org.apache.catalina.session.*;

import java.net.*;
import java.io.*;

/**
 * de.codewave.embedtomcat.EmbeddedTomcat
 */
public class EmbeddedTomcat {
    public static Embedded createServer(String name, InetAddress listenAddress, int listenPort, File catalinaBasePath, String webAppName,
            String webAppContext) throws IOException {
        Embedded server = new Embedded();
        server.setCatalinaBase(catalinaBasePath.getCanonicalPath());
        Engine engine = server.createEngine();
        engine.setName("engine." + name);
        engine.setDefaultHost("host." + name);
        Host host = server.createHost("host." + name, new File(catalinaBasePath, "webapps").getCanonicalPath());
        engine.addChild(host);
        Context context = server.createContext(webAppContext, webAppName);
        StandardManager sessionManager = new StandardManager();
        sessionManager.setPathname("");
        context.setManager(sessionManager);
        host.addChild(context);
        server.addEngine(engine);
        server.addConnector(server.createConnector(listenAddress, listenPort, false));
        return server;
    }
}