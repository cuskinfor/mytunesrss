/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.utils.servlet.StreamSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StatusCodeSender extends StreamSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCodeSender.class);
    
    private int myStatusCode;
    private String myMessage;

    public StatusCodeSender(int statusCode) {
        super(null, null, -1);
        myStatusCode = statusCode;
    }

    public StatusCodeSender(int statusCode, String message) {
        super(null, null, -1);
        myStatusCode = statusCode;
        myMessage = message;
    }

    @Override
    public void sendGetResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse, boolean throwException) {
        try {
            if (myMessage == null) {
                httpServletResponse.sendError(myStatusCode);
            } else {
                httpServletResponse.sendError(myStatusCode, myMessage);
            }
        } catch (IOException e) {
            LOGGER.error("Could not send status code response.", e);
            httpServletResponse.setStatus(myStatusCode);
        }
    }

    @Override
    public void sendHeadResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) {
        try {
            if (myMessage == null) {
                httpServletResponse.sendError(myStatusCode);
            } else {
                httpServletResponse.sendError(myStatusCode, myMessage);
            }
        } catch (IOException e) {
            LOGGER.error("Could not send status code response.", e);
            httpServletResponse.setStatus(myStatusCode);
        }
    }
}
