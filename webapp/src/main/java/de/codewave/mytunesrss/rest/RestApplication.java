/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.rest.resource.*;

import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(new Class<?>[]{
                MethodConstraintViolationExceptionMapper.class,
                AuthInterceptor.class,
                LibraryResource.class,
                PlaylistResource.class,
                ArtistResource.class,
                GenreResource.class,
                SessionResource.class,
                TvShowResource.class,
                AlbumResource.class}));
    }
}
