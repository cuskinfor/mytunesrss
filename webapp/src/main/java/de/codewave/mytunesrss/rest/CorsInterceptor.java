/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterContext;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@ServerInterceptor
public class CorsInterceptor implements MessageBodyWriterInterceptor {

    @Context
    private HttpServletRequest myRequest;

    public void write(MessageBodyWriterContext messageBodyWriterContext) throws IOException, WebApplicationException {
        messageBodyWriterContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        String origin = myRequest.getHeader("origin");
        messageBodyWriterContext.getHeaders().add("Access-Control-Allow-Origin", StringUtils.defaultIfBlank(origin, "*"));
        messageBodyWriterContext.proceed();
    }

}
