/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.List;

@Provider
@ServerInterceptor
public class FetchOffsetAndSizeInterceptor implements PostProcessInterceptor, AcceptedByMethod {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchOffsetAndSizeInterceptor.class);

    @Context
    private HttpServletRequest myRequest;

    @Override
    public void postProcess(ServerResponse response) {
        Object entity = response.getEntity();
        if (entity != null) {
            Long fetchOffset = null;
            try {
                fetchOffset = myRequest.getParameter("fetch.offset") != null ? Long.parseLong(myRequest.getParameter("fetch.offset")) : null;
            } catch (NumberFormatException ignored) {
                // ignore parse exception, just keep fetchOffset NULL
            }
            Long fetchSize = null;
            try {
                fetchSize = myRequest.getParameter("fetch.size") != null ? Long.parseLong(myRequest.getParameter("fetch.size")) : null;
            } catch (NumberFormatException ignored) {
                // ignore parse exception, just keep fetchSize NULL
            }
            if (fetchOffset != null || fetchSize != null) {
                if (List.class.isAssignableFrom(entity.getClass())) {
                    List list = (List) entity;
                    LOGGER.debug("Using fetch offset " + fetchOffset + " and fetch size " + fetchSize + " on a list with " + list.size() + " elements.");
                    long offset = fetchOffset != null ? fetchOffset : 0;
                    long size = fetchSize != null ? fetchSize : Long.MAX_VALUE;
                    response.getMetadata().add("X-MyTunesRSS-TotalNumberOfElements", list.size());
                    for (int i = 0; i < offset && !list.isEmpty(); i++) {
                        list.remove(0);
                    }
                    while (list.size() > size) {
                        list.remove(list.size() - 1);
                    }
                } else {
                    throw new RuntimeException("This interceptor cannot handle entities of type \"" + entity.getClass() + "\".");
                }
            }
        }
    }

    @Override
    public boolean accept(Class declaring, Method method) {
        return List.class.isAssignableFrom(method.getReturnType());
    }
}
