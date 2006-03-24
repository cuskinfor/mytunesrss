/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.embedtomcat.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    public static void main(String[] args) throws LifecycleException {
        int listenPort = Integer.parseInt(System.getProperty("listenPort"));
        Embedded server = EmbeddedTomcat.createServer("MyTunesRss", null, listenPort, new File("webapps"), "ROOT", "");
        server.start();
    }
}