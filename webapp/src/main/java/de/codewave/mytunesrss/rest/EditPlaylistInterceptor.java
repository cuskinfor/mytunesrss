/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.rest.resource.EditPlaylistResource;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

@Provider
@ServerInterceptor
public class EditPlaylistInterceptor implements PreProcessInterceptor, AcceptedByMethod {

    @Context
    private HttpServletRequest myRequest;

    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) {
        if (myRequest.getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST) == null) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_PRECONDITION_FAILED, "NOT_EDITING_PLAYLIST");
        }
        return null;
    }

    public boolean accept(Class declaring, Method method) {
        return declaring.equals(EditPlaylistResource.class);
    }
}
