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

    private static final int READ_TIMEOUT = 10000;

    public static UpdateInfo UPDATE_INFO;

    public void run() {
        Proxy proxy = null;
        if (MyTunesRss.CONFIG.isProxyServer()) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(MyTunesRss.CONFIG.getProxyHost(), MyTunesRss.CONFIG.getProxyPort()));
        }
        UPDATE_INFO = NetworkUtils.getCurrentUpdateInfo(MyTunesRss.UPDATE_URL, READ_TIMEOUT, proxy);
    }
}
