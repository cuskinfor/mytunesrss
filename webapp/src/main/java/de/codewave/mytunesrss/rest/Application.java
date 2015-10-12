package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.rest.resource.*;

import java.util.HashSet;
import java.util.Set;

public class Application extends javax.ws.rs.core.Application {

    private Set<Class<?>> myClasses = new HashSet<>();

    public Application() {
        myClasses.add(AlbumResource.class);
        myClasses.add(ArtistResource.class);
        myClasses.add(EditPlaylistResource.class);
        myClasses.add(GenreResource.class);
        myClasses.add(LibraryResource.class);
        myClasses.add(MediaPlayerResource.class);
        myClasses.add(PhotoAlbumResource.class);
        myClasses.add(PhotoResource.class);
        myClasses.add(PlaylistResource.class);
        myClasses.add(SessionResource.class);
        myClasses.add(TvShowResource.class);

        myClasses.add(AuthInterceptor.class);
        myClasses.add(CacheControlInterceptor.class);
        myClasses.add(CorsInterceptor.class);
        myClasses.add(EditPlaylistInterceptor.class);
        myClasses.add(FetchOffsetAndSizeInterceptor.class);
        myClasses.add(IncludeExcludeInterceptor.class);

        myClasses.add(MethodConstraintViolationExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return myClasses;
    }
}
