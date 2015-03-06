/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@Provider
@ServerInterceptor
public class CorsInterceptor implements PostProcessInterceptor {

    @Context
    private HttpServletRequest myRequest;

    @Override
    public void postProcess(ServerResponse serverResponse) {
        String origin = myRequest.getHeader("origin");
        if (StringUtils.isNotBlank(origin) && !"null".equalsIgnoreCase(origin)) {
            serverResponse.getMetadata().add("Access-Control-Allow-Credentials", "true");
            serverResponse.getMetadata().add("Access-Control-Allow-Origin", origin);
        }
    }
}
