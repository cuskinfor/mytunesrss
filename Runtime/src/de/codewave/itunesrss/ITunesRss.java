/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.itunesrss;

import de.codewave.embedtomcat.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.*;

import java.io.*;

/**
 * de.codewave.itunesrss.ITunesRss
 */
public class ITunesRss {
    public static void main(String[] args) throws LifecycleException {
        int listenPort = Integer.parseInt(System.getProperty("listenPort"));
        Embedded server = EmbeddedTomcat.createServer("iTunesRss", null, listenPort, new File("webapps"), "ROOT", "");
        server.start();
    }
}