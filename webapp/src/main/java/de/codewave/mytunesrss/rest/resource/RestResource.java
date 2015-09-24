/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.rest.CacheControlInterceptor;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.representation.*;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.sql.SQLException;

public class RestResource {

    protected QueryResultIterable<Artist, ArtistRepresentation> toArtistRepresentations(final UriInfo uriInfo, final HttpServletRequest request, QueryResult<Artist> artists) {
        return new QueryResultIterable<>(artists, new QueryResultIterable.ResultTransformer<Artist, ArtistRepresentation>() {
            @Override
            public ArtistRepresentation transform(Artist artist) {
                return toArtistRepresentation(uriInfo, request, artist);
            }
        });
    }

    protected ArtistRepresentation toArtistRepresentation(UriInfo uriInfo, HttpServletRequest request, Artist artist) {
        ArtistRepresentation representation = new ArtistRepresentation(artist);
        if (IncludeExcludeInterceptor.isAttr("albumsUri")) {
            representation.setAlbumsUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getAlbums").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(artist.getName())).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("tracksUri")) {
            representation.setTracksUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(artist.getName())).toString());
        }
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (user.isPlaylist()) {
            if (IncludeExcludeInterceptor.isAttr("m3uUri")) {
                representation.setM3uUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc("artist=" + b64(artist.getName())), enc("type=M3u"), enc("_cdi=" + ue(artist.getName()) + ".m3u")).toString());
            }
            if (IncludeExcludeInterceptor.isAttr("xspfUri")) {
                representation.setXspfUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc("artist=" + b64(artist.getName())), enc("type=Xspf"), enc("_cdi=" + ue(artist.getName()) + ".xspf")).toString());
            }
        }
        if (IncludeExcludeInterceptor.isAttr("rssUri") && user.isRss()) {
            representation.setRssUri(getAppURI(request, MyTunesRssCommand.CreateRss, enc("artist=" + b64(artist.getName())), enc("_cdi=" + ue(artist.getName()) + ".xml")).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("downloadUri") && user.isDownload()) {
            representation.setDownloadUri(getAppURI(request, MyTunesRssCommand.GetZipArchive, enc("artist=" + b64(artist.getName())), enc("_cda=" + ue(artist.getName()) + ".zip")).toString());
        }
        return representation;
    }

    protected QueryResultIterable<Album, AlbumRepresentation> toAlbumRepresentations(final UriInfo uriInfo, final HttpServletRequest request, QueryResult<Album> albums) {
        return new QueryResultIterable<>(albums, new QueryResultIterable.ResultTransformer<Album, AlbumRepresentation>() {
            @Override
            public AlbumRepresentation transform(Album album) {
                return toAlbumRepresentation(uriInfo, request, album);
            }
        });
    }

    protected AlbumRepresentation toAlbumRepresentation(UriInfo uriInfo, HttpServletRequest request, Album album) {
        AlbumRepresentation representation = new AlbumRepresentation(album);
        if (IncludeExcludeInterceptor.isAttr("tracksUri")) {
            representation.setTracksUri(uriInfo.getBaseUriBuilder().path(AlbumResource.class).path(AlbumResource.class, "getAlbumTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist()), MiscUtils.getUtf8UrlEncoded(album.getName())).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("artistUri")) {
            representation.setArtistUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist())).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("imageUri") && StringUtils.isNotBlank(album.getImageHash())) {
            representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc("hash=" + album.getImageHash())).toString());
        }
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (user.isPlaylist()) {
            if (IncludeExcludeInterceptor.isAttr("m3uUri")) {
                representation.setM3uUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc("album=" + b64(album.getName())), enc("type=M3u"), enc("_cdi=" + ue(album.getName()) + ".m3u")).toString());
            }
            if (IncludeExcludeInterceptor.isAttr("xspfUri")) {
                representation.setXspfUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc("album=" + b64(album.getName())), enc("type=Xspf"), enc("_cdi=" + ue(album.getName()) + ".xspf")).toString());
            }
        }
        if (IncludeExcludeInterceptor.isAttr("rssUri") && user.isRss()) {
            representation.setRssUri(getAppURI(request, MyTunesRssCommand.CreateRss, enc("album=" + b64(album.getName())), enc("_cdi=" + ue(album.getName()) + ".xml")).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("downloadUri") && user.isDownload()) {
            representation.setDownloadUri(getAppURI(request, MyTunesRssCommand.GetZipArchive, enc("album=" + b64(album.getName())), enc("albumartist=" + b64(album.getArtist())), enc("_cda=" + ue(album.getName()) + ".zip")).toString());
        }
        return representation;
    }

    protected QueryResultIterable<Genre, GenreRepresentation> toGenreRepresentations(final UriInfo uriInfo, QueryResult<Genre> genres) {
        return new QueryResultIterable<>(genres, new QueryResultIterable.ResultTransformer<Genre, GenreRepresentation>() {
            @Override
            public GenreRepresentation transform(Genre genre) {
                return toGenreRepresentation(uriInfo, genre);
            }
        });
    }

    protected GenreRepresentation toGenreRepresentation(UriInfo uriInfo, Genre genre) {
        GenreRepresentation representation = new GenreRepresentation(genre);
        if (IncludeExcludeInterceptor.isAttr("tracksUri")) {
            representation.setTracksUri(uriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("albumsUri")) {
            representation.setAlbumsUri(uriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreAlbums").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("artistsUri")) {
            representation.setArtistsUri(uriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreArtists").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())).toString());
        }
        return representation;
    }

    protected QueryResultIterable<Playlist, PlaylistRepresentation> toPlaylistRepresentations(final UriInfo uriInfo, final HttpServletRequest request, QueryResult<Playlist> playlists) {
        return new QueryResultIterable<>(playlists, new QueryResultIterable.ResultTransformer<Playlist, PlaylistRepresentation>() {
            @Override
            public PlaylistRepresentation transform(Playlist playlist) {
                return toPlaylistRepresentation(uriInfo, request, playlist);
            }
        });
    }

    protected PlaylistRepresentation toPlaylistRepresentation(UriInfo uriInfo, HttpServletRequest request, Playlist playlist) {
        PlaylistRepresentation representation = new PlaylistRepresentation(playlist);
        Playlist currentlyEditedPlaylist = (Playlist) request.getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST);
        if (IncludeExcludeInterceptor.isAttr("tracksUri")) {
            if (playlist == currentlyEditedPlaylist) {
                representation.setTracksUri(uriInfo.getBaseUriBuilder().path(EditPlaylistResource.class).path(EditPlaylistResource.class, "getPlaylistTracks").build().toString());
            } else {
                representation.setTracksUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTracks").build(playlist.getId()).toString());
            }
        }
        if (playlist.getId() != null) {
            // persistent playlist
            if (IncludeExcludeInterceptor.isAttr("childrenUri")) {
                representation.setChildrenUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getPlaylistChildren").build(playlist.getId()).toString());
            }
            if (IncludeExcludeInterceptor.isAttr("parentUri") && StringUtils.isNotBlank(playlist.getContainerId())) {
                representation.setParentUri(uriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getPlaylist").build(playlist.getContainerId()).toString());
            }
            if (IncludeExcludeInterceptor.isAttr("downloadUri") && MyTunesRssWebUtils.getAuthUser(request).isDownload()) {
                representation.setDownloadUri(getAppURI(request, MyTunesRssCommand.GetZipArchive, enc("playlist=" + playlist.getId()), enc("_cda=" + ue(playlist.getName()) + ".zip")).toString());
            }
        }
        return representation;
    }

    protected QueryResultIterable<Track, TrackRepresentation> toTrackRepresentations(final UriInfo uriInfo, final HttpServletRequest request, QueryResult<Track> tracks) {
        return new QueryResultIterable<>(tracks, new QueryResultIterable.ResultTransformer<Track, TrackRepresentation>() {
            @Override
            public TrackRepresentation transform(Track track) {
                return toTrackRepresentation(uriInfo, request, track);
            }
        });
    }

    protected TrackRepresentation toTrackRepresentation(UriInfo uriInfo, HttpServletRequest request, Track track) {
        TrackRepresentation representation = new TrackRepresentation(track);
        if (IncludeExcludeInterceptor.isAttr("imageUri") && StringUtils.isNotBlank(track.getImageHash())) {
            representation.setImageUri(getAppURI(request, MyTunesRssCommand.ShowImage, enc("hash=" + track.getImageHash())).toString());
        }
        User user = MyTunesRssWebUtils.getAuthUser(request);
        if (user.isPlaylist()) {
            if (IncludeExcludeInterceptor.isAttr("m3uUri")) {
                representation.setM3uUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc("track=" + track.getId()), enc("type=M3u"), enc("_cdi=" + ue(track.getName()) + ".m3u")).toString());
            }
            if (IncludeExcludeInterceptor.isAttr("xspfUri")) {
                representation.setXspfUri(getAppURI(request, MyTunesRssCommand.CreatePlaylist, enc("track=" + track.getId()), enc("type=Xspf"), enc("_cdi=" + ue(track.getName()) + ".xspf")).toString());
            }
        }
        if (IncludeExcludeInterceptor.isAttr("rssUri") && user.isRss()) {
            representation.setRssUri(getAppURI(request, MyTunesRssCommand.CreateRss, enc("track=" + track.getId()), enc("_cdi=" + ue(track.getName()) + ".xml")).toString());
        }
        request.setAttribute("downloadPlaybackServletUrl", MyTunesRssWebUtils.getServletUrl(request)); // prepare MyTunesFunctions
        if (IncludeExcludeInterceptor.isAttr("downloadUri") && user.isDownload()) {
            representation.setDownloadUri(UriBuilder.fromUri(MyTunesFunctions.downloadUrl(request, track, null)).build().toString());
        }
        if (IncludeExcludeInterceptor.isAttr("httpLiveStreamUri") && MyTunesRssWebUtils.isHttpLiveStreaming(request, track, true, true)) {
            representation.setHttpLiveStreamUri(UriBuilder.fromUri(MyTunesFunctions.httpLiveStreamUrl(request, track, null)).build().toString());
        }
        if (IncludeExcludeInterceptor.isAttr("playbackUri")) {
            representation.setPlaybackUri(UriBuilder.fromUri(MyTunesFunctions.playbackUrl(request, track, null)).build().toString());
        }
        if (IncludeExcludeInterceptor.isAttr("artistUri")) {
            representation.setArtistUri(uriInfo.getBaseUriBuilder().path(ArtistResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(track.getArtist())).toString());
        }
        if (IncludeExcludeInterceptor.isAttr("albumUri")) {
            representation.setAlbumUri(uriInfo.getBaseUriBuilder().path(AlbumResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(track.getAlbumArtist()), MiscUtils.getUtf8UrlEncoded(track.getAlbum())).toString());
        }
        return representation;
    }

    public URI getAppURI(HttpServletRequest request, MyTunesRssCommand command, String... paths) {
        UriBuilder uriBuilder = UriBuilder.fromUri(MyTunesRssWebUtils.getCommandCall(request, command));
        User authUser = MyTunesRssWebUtils.getAuthUser(request);
        if (authUser != null) {
            uriBuilder.path(MyTunesRssUtils.encryptPathInfo("auth=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssBase64Utils.encode(authUser.getName()) + " " + MyTunesRssBase64Utils.encode(authUser.getPasswordHash()))));
        }
        for (String path : paths) {
            uriBuilder.path(path);
        }
        return uriBuilder.build();
    }

    protected String enc(String s) {
        return MyTunesRssUtils.encryptPathInfo(s);
    }

    protected String b64(String s) {
        return MyTunesRssBase64Utils.encode(s);
    }

    protected String ue(String s) {
        return MiscUtils.getUtf8UrlEncoded(s);
    }

    /**
     * @exclude no swagger docs
     */
    @OPTIONS
    @Path("/{path:.*}")
    public Response handleCorsOptions(@HeaderParam("Access-Control-Request-Method") String method, @HeaderParam("Access-Control-Request-Headers") String headers) {
        Response.ResponseBuilder response = Response.ok();
        if (StringUtils.isNotBlank(method)) {
            response.header("Access-Control-Allow-Methods", method);
        }
        if (StringUtils.isNotBlank(headers)) {
            response.header("Access-Control-Allow-Headers", headers);
        }
        return response.build();
    }

    protected void handleDatabaseLastModified() throws SQLException {
        long lastDatabaseUpdate = TransactionFilter.getTransaction().executeQuery(new GetSystemInformationQuery()).getLastUpdate();
        handleLastModified(lastDatabaseUpdate);
    }

    protected void handleLastModified(long lastModified) {
        if (CacheControlInterceptor.getIfModifiedSince() != null && (lastModified / 1000) <= (CacheControlInterceptor.getIfModifiedSince() / 1000)) {
            throw new MyTunesRssRestException(HttpServletResponse.SC_NOT_MODIFIED, null);
        }
        CacheControlInterceptor.setLastModified(lastModified);
    }
}
