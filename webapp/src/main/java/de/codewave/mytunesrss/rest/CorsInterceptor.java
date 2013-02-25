/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterContext;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@ServerInterceptor
public class CorsInterceptor implements MessageBodyWriterInterceptor {

    public void write(MessageBodyWriterContext messageBodyWriterContext) throws IOException, WebApplicationException {
        messageBodyWriterContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        messageBodyWriterContext.getHeaders().add("Access-Control-Allow-Origin", "*");
    }

}
