/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.rest.representation.RestRepresentation;
import org.apache.commons.collections.CollectionUtils;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

@Provider
@ServerInterceptor
public class IncludeExcludeInterceptor implements PostProcessInterceptor, AcceptedByMethod {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncludeExcludeInterceptor.class);

    @Context
    private HttpServletRequest myRequest;

    public void postProcess(ServerResponse response) {
        Object entity = response.getEntity();
        List<String> includes = myRequest.getParameterValues("attr.incl") != null ? Arrays.asList(myRequest.getParameterValues("attr.incl")) : null;
        List<String> excludes = myRequest.getParameterValues("attr.excl") != null ? Arrays.asList(myRequest.getParameterValues("attr.excl")) : null;
        if (CollectionUtils.isNotEmpty(includes) || CollectionUtils.isNotEmpty(excludes)) {
            if (entity instanceof RestRepresentation) {
                handleRepresentation((RestRepresentation) entity, includes, excludes);
            } else if (entity instanceof List) {
                handleList((List) entity, includes, excludes);
            }
        }
    }

    private void handleList(List items, List<String> includes, List<String> excludes) {
        for (Iterator iter = items.iterator(); iter.hasNext(); ) {
            Object item = iter.next();
            if (item instanceof RestRepresentation) {
                handleRepresentation((RestRepresentation) item, includes, excludes);
            } else if (items instanceof Collection) {
                handleList((List) item, includes, excludes);
            }
        }
    }

    private void handleRepresentation(RestRepresentation item, List<String> includes, List<String> excludes) {
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(item.getClass()).getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && item.getClass().equals(pd.getReadMethod().getDeclaringClass()) && List.class.isAssignableFrom(pd.getPropertyType())) {
                    handleList((List) pd.getReadMethod().invoke(item, new Object[0]), includes, excludes);
                }
                if (CollectionUtils.isNotEmpty(excludes) && excludes.contains(pd.getName()) || (CollectionUtils.isNotEmpty(includes) && !includes.contains(pd.getName()))) {
                    if (pd.getWriteMethod() != null && item.getClass().equals(pd.getWriteMethod().getDeclaringClass())) {
                        try {
                            pd.getWriteMethod().invoke(item, new Object[] {null});
                        } catch (Exception e) {
                            LOGGER.info("Could not remove property \"" + pd.getName() + "\" from representation.", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Could not remove properties from representation.", e);
        }
    }

    public boolean accept(Class declaring, Method method) {
        return !method.getReturnType().equals(Void.class);
    }
}
