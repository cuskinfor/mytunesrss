/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ShortenUrlCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortenUrlCommandHandler.class);

    @Override
    public void executeAuthorized() throws IOException {
        String url = getRequestParameter("url", null);
        if (url != null) {
            HttpClient client = new HttpClient();
            PostMethod postMethod = new PostMethod("http://goo.gl/api/shorten");
            try {
                postMethod.addParameter("url", url);
                client.executeMethod(postMethod);
                if (postMethod.getStatusCode() == 201) {
                    getResponse().getWriter().print(postMethod.getResponseHeader("Location").getValue());
                }
            } catch (IOException e) {
                LOGGER.error("Could not shorten URL.", e);
                getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } finally {
                postMethod.releaseConnection();
            }
        } else {
            getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing URL parameter.");
        }
    }
}
