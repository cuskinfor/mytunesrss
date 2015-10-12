/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.ws.rs.ext.Provider;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Provider
@ServerInterceptor
public class CacheControlInterceptor implements PreProcessInterceptor, PostProcessInterceptor {

    private static final ThreadLocal<Long> LAST_MODIFIED = new ThreadLocal<>();
    private static final ThreadLocal<Long> IF_MODIFIED_SINCE = new ThreadLocal<>();
    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format;
        }
    };

    public static void setLastModified(long lastModified) {
        LAST_MODIFIED.set(lastModified);
    }

    public static Long getIfModifiedSince() {
        return IF_MODIFIED_SINCE.get();
    }

    @Override
    public ServerResponse preProcess(HttpRequest httpRequest, ResourceMethod method) {
        IF_MODIFIED_SINCE.remove();
        List<String> header = httpRequest.getHttpHeaders().getRequestHeader("If-Modified-Since");
        if (header != null && !header.isEmpty()) {
            try {
                IF_MODIFIED_SINCE.set(DATE_FORMAT.get().parse(header.get(0)).getTime());
            } catch (ParseException ignored) {
                // ignore parse exception
            }
        }
        return null;
    }

    @Override
    public void postProcess(ServerResponse serverResponse) {
        try {
            if (LAST_MODIFIED.get() != null) {
                serverResponse.getMetadata().add("Last-Modified", DATE_FORMAT.get().format(new Date(LAST_MODIFIED.get())));
            }
        } finally {
            LAST_MODIFIED.remove();
        }
        serverResponse.getMetadata().add("Expires", DATE_FORMAT.get().format(new Date(0)));
        serverResponse.getMetadata().add("Cache-Control", MyTunesRssWebUtils.createCacheControlValue(0));
    }

}
