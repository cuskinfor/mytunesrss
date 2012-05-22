/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.User;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@ServerInterceptor
public class AuthInterceptor implements PreProcessInterceptor {

    @Context private HttpServletRequest myRequest;

    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        User user = MyTunesRssWebUtils.getAuthUser(myRequest);
        if (user == null) {
            return ServerResponse.copyIfNotServerResponse(Response.status(HttpServletResponse.SC_UNAUTHORIZED).build());
        }
        return null;
    }
}
