/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesFunctions;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.rest.representation.*;
import de.codewave.utils.MiscUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RestResource {

    @Context
    private UriInfo myUriInfo;

    @Context
    private HttpServletRequest myRequest;

    protected User getAuthUser() {
        return MyTunesRssWebUtils.getAuthUser(myRequest);
    }

    protected List<ArtistRepresentation> toArtistRepresentations(List<Artist> artists) {
        List<ArtistRepresentation> representations = new ArrayList<ArtistRepresentation>();
        for (Artist artist : artists) {
            representations.add(toArtistRepresentation(artist));
        }
        return representations;
    }

    protected ArtistRepresentation toArtistRepresentation(Artist artist) {
        ArtistRepresentation representation = new ArtistRepresentation(artist);
        representation.getUri().put("albums", myUriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getAlbums").build(MyTunesRssBase64Utils.encode(artist.getName())));
        if (getAuthUser().isPlaylist()) {
            representation.getUri().put("m3u", getAppURI(MyTunesRssCommand.CreatePlaylist, "artist=" + artist.getName(), "type=M3u"));
            representation.getUri().put("xspf", getAppURI(MyTunesRssCommand.CreatePlaylist, "artist=" + artist.getName(), "type=Xspf"));
        }
        if (getAuthUser().isRss()) {
            representation.getUri().put("rss", getAppURI(MyTunesRssCommand.CreateRss, "artist=" + artist.getName()));
        }
        if (getAuthUser().isDownload()) {
            representation.getUri().put("download", getAppURI(MyTunesRssCommand.GetZipArchive, "artist=" + artist.getName()));
        }
        return representation;
    }

    protected List<AlbumRepresentation> toAlbumRepresentations(List<Album> albums) {
        List<AlbumRepresentation> representations = new ArrayList<AlbumRepresentation>();
        for (Album album : albums) {
            representations.add(toAlbumRepresentation(album));
        }
        return representations;
    }

    protected AlbumRepresentation toAlbumRepresentation(Album album) {
        AlbumRepresentation representation = new AlbumRepresentation(album);
        representation.getUri().put("tracks", myUriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getArtistAlbumTracks").build(MyTunesRssBase64Utils.encode(album.getArtist()), MyTunesRssBase64Utils.encode(album.getName())));
        representation.getUri().put("artist", myUriInfo.getBaseUriBuilder().path(ArtistResource.class).build(MyTunesRssBase64Utils.encode(album.getArtist()), MyTunesRssBase64Utils.encode(album.getName())));
        if (StringUtils.isNotBlank(album.getImageHash())) {
            representation.getUri().put("image", getAppURI(MyTunesRssCommand.ShowImage, "hash=" + album.getImageHash()));
        }
        if (getAuthUser().isPlaylist()) {
            representation.getUri().put("m3u", getAppURI(MyTunesRssCommand.CreatePlaylist, "album=" + album.getName(), "type=M3u"));
            representation.getUri().put("xspf", getAppURI(MyTunesRssCommand.CreatePlaylist, "album=" + album.getName(), "type=Xspf"));
        }
        if (getAuthUser().isRss()) {
            representation.getUri().put("rss", getAppURI(MyTunesRssCommand.CreateRss, "album=" + album.getName()));
        }
        if (getAuthUser().isDownload()) {
            representation.getUri().put("download", getAppURI(MyTunesRssCommand.GetZipArchive, "album=" + album.getName(), "albumartist=" + album.getArtist()));
        }
        return representation;
    }

    protected List<GenreRepresentation> toGenreRepresentations(List<Genre> genres) {
        List<GenreRepresentation> representations = new ArrayList<GenreRepresentation>();
        for (Genre genre : genres) {
            representations.add(toGenreRepresentation(genre));
        }
        return representations;
    }

    protected GenreRepresentation toGenreRepresentation(Genre genre) {
        GenreRepresentation representation = new GenreRepresentation(genre);
        representation.getUri().put("tracks", myUriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreTracks").build(MyTunesRssBase64Utils.encode(genre.getName())));
        return representation;
    }

    protected List<PlaylistRepresentation> toPlaylistRepresentations(List<Playlist> playlists) {
        List<PlaylistRepresentation> representations = new ArrayList<PlaylistRepresentation>();
        for (Playlist playlist : playlists) {
            representations.add(toPlaylistRepresentation(playlist));
        }
        return representations;
    }

    protected PlaylistRepresentation toPlaylistRepresentation(Playlist playlist) {
        PlaylistRepresentation representation = new PlaylistRepresentation(playlist);
        representation.getUri().put("tracks", myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTracks").build(playlist.getId()));
        representation.getUri().put("children", myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getPlaylistChildren").build(playlist.getId()));
        if (StringUtils.isNotBlank(playlist.getContainerId())) {
            representation.getUri().put("parent", myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).build(playlist.getContainerId()));
        }
        representation.getUri().put("tags", myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTags").build(playlist.getId()));
        if (getAuthUser().isDownload()) {
            representation.getUri().put("download", getAppURI(MyTunesRssCommand.GetZipArchive, "playlist=" + playlist.getId()));
        }
        return representation;
    }

    protected List<TrackRepresentation> toTrackRepresentations(List<Track> tracks) {
        List<TrackRepresentation> representations = new ArrayList<TrackRepresentation>();
        for (Track track : tracks) {
            representations.add(toTrackRepresentation(track));
        }
        return representations;
    }

    protected TrackRepresentation toTrackRepresentation(Track track) {
        TrackRepresentation representation = new TrackRepresentation(track);
        if (StringUtils.isNotBlank(track.getImageHash())) {
            representation.getUri().put("image", getAppURI(MyTunesRssCommand.ShowImage, "hash=" + track.getImageHash()));
        }
        if (getAuthUser().isPlaylist()) {
            representation.getUri().put("m3u", getAppURI(MyTunesRssCommand.CreatePlaylist, "track=" + track.getId(), "type=M3u"));
            representation.getUri().put("xspf", getAppURI(MyTunesRssCommand.CreatePlaylist, "track=" + track.getId(), "type=Xspf"));
        }
        if (getAuthUser().isRss()) {
            representation.getUri().put("rss", getAppURI(MyTunesRssCommand.CreateRss, "track=" + track.getId()));
        }
        if (getAuthUser().isDownload()) {
            representation.getUri().put("download", UriBuilder.fromUri(MyTunesFunctions.downloadUrl(myRequest, track, null)).build());
        }
        if (MyTunesRssWebUtils.isHttpLiveStreaming(myRequest, track, true)) {
            representation.getUri().put("playback", UriBuilder.fromUri(MyTunesFunctions.playbackUrl(myRequest, track, null)).build());
        } else {
            representation.getUri().put("playback", UriBuilder.fromUri(MyTunesFunctions.httpLiveStreamUrl(myRequest, track, null)).build());
        }
        return representation;
    }

    public URI getAppURI(MyTunesRssCommand command, String... paths) {
        UriBuilder uriBuilder = UriBuilder.fromUri(MyTunesRssWebUtils.getCommandCall(myRequest, command));
        User authUser = getAuthUser();
        if (authUser != null) {
            uriBuilder.path(MyTunesRssWebUtils.encryptPathInfo(myRequest, "auth=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssBase64Utils.encode(authUser.getName()) + " " + MyTunesRssBase64Utils.encode(authUser.getPasswordHash()))));
        }
        for (String path : paths) {
            uriBuilder.path(MyTunesRssWebUtils.encryptPathInfo(myRequest, path));
        }
        return uriBuilder.build();
    }
}
