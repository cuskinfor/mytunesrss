/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.network;

import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * de.codewave.utils.network.NetworkUtils
 */
public class NetworkUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUtils.class);

    public static UpdateInfo getCurrentUpdateInfo(HttpClient httpClient, String updateCheckUrl) {
        if (httpClient != null && updateCheckUrl != null) {
            GetMethod getMethod = new GetMethod(updateCheckUrl);
            try {
                if (httpClient.executeMethod(getMethod) == 200) {
                    return new UpdateInfo(JXPathUtils.getContext(new String(getMethod.getResponseBody(), "UTF-8")));
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not retrieve update info, HTTP response code " + getMethod.getStatusCode() + ".");
                    }

                }
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not retrieve update info.", e);
                }
            } finally {
                getMethod.releaseConnection();
            }
        }
        return null;
    }

    /**
     * Get a list of all local network addresses. The list contains only addresses that will work from other computers on the same network, i.e. the
     * special 127.0.0.1 is not returned.
     *
     * @return An array of all local network addresses. The method might return an empty array but never <code>null</code>.
     */
    public static String[] getLocalNetworkAddresses() {
        List<String> localAddresses = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intFace = interfaces.nextElement();
                Enumeration<InetAddress> addresses = intFace.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        localAddresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not get network interfaces.", e);
            }
        }
        return localAddresses.toArray(new String[localAddresses.size()]);
    }

    /**
     * Check if the specified network address is a local address, i.e. it is either 127.0.0.1 or any of the addresses returned from {@link
     * #getLocalNetworkAddresses()}.
     *
     * @param address A network address.
     * @return <code>true</code> if the address is local or <code>false</code> otherwise.
     */
    public static boolean isLocalAddress(String address) {
        if (StringUtils.isNotEmpty(address)) {
            if (address.equals("127.0.0.1")) {
                return true;
            }
            for (String localAddress : getLocalNetworkAddresses()) {
                if (address.equals(localAddress)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Downloader createDownloader(URL url, File target, DownloadProgressListener listener) {
        return new Downloader(url, target, listener);
    }
}
