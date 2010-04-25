/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class FetchExternalAddressRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FetchExternalAddressRunnable.class);

    public static String EXTERNAL_ADDRESS;

    public void run() {
        if (MyTunesRss.WEBSERVER.isRunning()) {
            BufferedReader reader = null;
            try {
                URLConnection connection = new URL("http://www.codewave.de/tools/getip.php").openConnection();
                if (connection != null) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    if (reader != null) {
                        EXTERNAL_ADDRESS = "http://" + reader.readLine() + ":" + MyTunesRss.CONFIG.getPort();
                        return;
                    }
                }
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not read my external address from \"www.codewave.de/tools/getip.php\".", e);
                }
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not close reader.", e);
                        }
                    }
                }
            }
        }
        EXTERNAL_ADDRESS = null;
    }
}
