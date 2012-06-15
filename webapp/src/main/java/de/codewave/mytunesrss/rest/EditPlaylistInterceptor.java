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
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@ServerInterceptor
public class EditPlaylistInterceptor implements PreProcessInterceptor {

    @Context
    private HttpServletRequest myRequest;

    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) {
        if (!method.getResourceClass().equals(EditPlaylistResource.class)) {
            return null;
        }
        if (myRequest.getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST) == null) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_PRECONDITION_FAILED, "NOT_EDITING_PLAYLIST");
        }
        return null;
    }
}