/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FetchExternalAddressRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FetchExternalAddressRunnable.class);
    private static final String GET_IP_URI = "http://ip4.telize.com";

    public static volatile String EXTERNAL_ADDRESS;

    private HttpClient myHttpClient = MyTunesRssUtils.createHttpClient();

    @Override
    public void run() {
        try {
            if (MyTunesRss.WEBSERVER.isRunning()) {
                GetMethod getMethod = new GetMethod(GET_IP_URI);
                try {
                    if (myHttpClient.executeMethod(getMethod) == 200) {
                        //noinspection AssignmentToStaticFieldFromInstanceMethod
                        EXTERNAL_ADDRESS = "http://" + StringUtils.trim(getMethod.getResponseBodyAsString()) + ":" + MyTunesRss.CONFIG.getPort();
                    }
                } catch (IOException e) {
                    LOG.debug("Could not read my external address from \"" + GET_IP_URI + "\".", e);
                } finally {
                    getMethod.releaseConnection();
                }
            } else {
                //noinspection AssignmentToStaticFieldFromInstanceMethod
                EXTERNAL_ADDRESS =  null;
            }
        } catch (RuntimeException e) {
            LOG.warn("Encountered unexpected exception. Caught to keep scheduled task alive.", e);
        }
    }
}
