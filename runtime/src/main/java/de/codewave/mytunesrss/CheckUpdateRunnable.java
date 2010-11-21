/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.network.NetworkUtils;
import de.codewave.utils.network.UpdateInfo;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class CheckUpdateRunnable implements Runnable {

    public static UpdateInfo UPDATE_INFO;

    public void run() {
        UPDATE_INFO = NetworkUtils.getCurrentUpdateInfo(MyTunesRssUtils.createHttpClient(), MyTunesRss.UPDATE_URL);
    }
}
