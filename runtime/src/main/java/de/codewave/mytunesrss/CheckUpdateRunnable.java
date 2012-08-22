/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.network.NetworkUtils;
import de.codewave.utils.network.UpdateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckUpdateRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckUpdateRunnable.class);

    public static UpdateInfo UPDATE_INFO;

    public void run() {
        try {
            UPDATE_INFO = NetworkUtils.getCurrentUpdateInfo(MyTunesRssUtils.createHttpClient(), MyTunesRss.UPDATE_URL);
        } catch (RuntimeException e) {
            LOGGER.warn("Encountered unexpected exception. Caught to keep scheduled task alive.", e);
        }
    }
}
