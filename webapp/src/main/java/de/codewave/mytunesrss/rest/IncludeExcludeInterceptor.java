/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Provider
@ServerInterceptor
public class IncludeExcludeInterceptor implements PreProcessInterceptor, AcceptedByMethod {

    private static final ThreadLocal<Set<String>> INCLUDES = new ThreadLocal<Set<String>>() {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };

    private static final ThreadLocal<Set<String>> EXCLUDES = new ThreadLocal<Set<String>>() {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };

    public static boolean isAttr(String name) {
        return (INCLUDES.get().isEmpty() || INCLUDES.get().contains(name)) && !EXCLUDES.get().contains(name);
    }

    @Context
    private HttpServletRequest myRequest;


    @Override
    public ServerResponse preProcess(HttpRequest httpRequest, ResourceMethod method) {
        INCLUDES.get().clear();
        if (myRequest.getParameterValues("attr.incl") != null) {
            INCLUDES.get().addAll(Arrays.asList(myRequest.getParameterValues("attr.incl")));
        }
        EXCLUDES.get().clear();
        if (myRequest.getParameterValues("attr.excl") != null) {
            EXCLUDES.get().addAll(Arrays.asList(myRequest.getParameterValues("attr.excl")));
        }
        return null;
    }

    @Override
    public boolean accept(Class declaring, Method method) {
        return !method.getReturnType().equals(Void.class);
    }
}
