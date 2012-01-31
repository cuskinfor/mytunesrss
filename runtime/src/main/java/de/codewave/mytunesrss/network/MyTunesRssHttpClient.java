/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.network;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.MiscUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MyTunesRssHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssHttpClient.class);
    private static final String GET_NICKNAME_URI = "http://mytunesrss.com/tools/get_nickname.php";

    public static String getMyTunesRssComNickname() {
        HttpClient client = MyTunesRssUtils.createHttpClient();
        PostMethod postMethod = new PostMethod(GET_NICKNAME_URI);
        postMethod.addParameter("user", MyTunesRss.CONFIG.getMyTunesRssComUser());
        postMethod.addParameter("pass", getMyTunesRssComPasswordParamValue());
        try {
            if (client.executeMethod(postMethod) == 200) {
                return postMethod.getResponseBodyAsString();
            }
        } catch (IOException e) {
            LOGGER.warn("Could not fetch mytunesrss.com nickname for \"" + MyTunesRss.CONFIG.getMyTunesRssComUser() + "\".");
        }
        return null;
    }

    private static String getMyTunesRssComPasswordParamValue() {
        return MiscUtils.getUtf8String(Base64.encodeBase64(MyTunesRss.CONFIG.getMyTunesRssComPasswordHash()));
    }

}
