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
    protected HttpServletRequest myRequest;

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
        representation.setAlbumsUri(myUriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getAlbums").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(artist.getName())));
        representation.setTagsUri(myUriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getTags").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(artist.getName())));
        if (getAuthUser().isPlaylist()) {
            representation.setM3uUri(getAppURI(MyTunesRssCommand.CreatePlaylist, enc("artist=" + b64(artist.getName())), enc("type=M3u"), fn(artist, "m3u")));
            representation.setXspfUri(getAppURI(MyTunesRssCommand.CreatePlaylist, enc("artist=" + b64(artist.getName())), enc("type=Xspf"), fn(artist, "xspf")));
        }
        if (getAuthUser().isRss()) {
            representation.setRssUri(getAppURI(MyTunesRssCommand.CreateRss, enc("artist=" + b64(artist.getName())), fn(artist, "rss")));
        }
        if (getAuthUser().isDownload()) {
            representation.setDownloadUri(getAppURI(MyTunesRssCommand.GetZipArchive, enc("artist=" + b64(artist.getName())), fn(artist, "zip")));
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
        representation.setTracksUri(myUriInfo.getBaseUriBuilder().path(AlbumResource.class).path(AlbumResource.class, "getAlbumTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist()), MiscUtils.getUtf8UrlEncoded(album.getName())));
        representation.setArtistUri(myUriInfo.getBaseUriBuilder().path(ArtistResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist())));
        representation.setTagsUri(myUriInfo.getBaseUriBuilder().path(AlbumResource.class).path(AlbumResource.class, "getTags").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(album.getArtist()), MiscUtils.getUtf8UrlEncoded(album.getName())));
        if (StringUtils.isNotBlank(album.getImageHash())) {
            representation.setImageUri(getAppURI(MyTunesRssCommand.ShowImage, enc("hash=" + album.getImageHash())));
        }
        if (getAuthUser().isPlaylist()) {
            representation.setM3uUri(getAppURI(MyTunesRssCommand.CreatePlaylist, enc("album=" + b64(album.getName())), enc("type=M3u"), fn(album, "m3u")));
            representation.setXspfUri(getAppURI(MyTunesRssCommand.CreatePlaylist, enc("album=" + b64(album.getName())), enc("type=Xspf"), fn(album, "xspf")));
        }
        if (getAuthUser().isRss()) {
            representation.setRssUri(getAppURI(MyTunesRssCommand.CreateRss, enc("album=" + b64(album.getName())), fn(album, "rss")));
        }
        if (getAuthUser().isDownload()) {
            representation.setDownloadUri(getAppURI(MyTunesRssCommand.GetZipArchive, enc("album=" + b64(album.getName())), enc("albumartist=" + b64(album.getArtist())), fn(album, "zip")));
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
        representation.setTracksUri(myUriInfo.getBaseUriBuilder().path(GenreResource.class).path(GenreResource.class, "getGenreTracks").buildFromEncoded(MiscUtils.getUtf8UrlEncoded(genre.getName())));
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
        Playlist currentlyEditedPlaylist = (Playlist) myRequest.getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST);
        if (playlist == currentlyEditedPlaylist) {
            representation.setTracksUri(myUriInfo.getBaseUriBuilder().path(EditPlaylistResource.class).path(EditPlaylistResource.class, "getPlaylistTracks").build());
        } else {
            representation.setTracksUri(myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTracks").build(playlist.getId()));
        }
        if (playlist.getId() != null) {
            // persistent playlist
            representation.setChildrenUri(myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getPlaylistChildren").build(playlist.getId()));
            if (StringUtils.isNotBlank(playlist.getContainerId())) {
                representation.setParentUri(myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).build(playlist.getContainerId()));
            }
            representation.setTagsUri(myUriInfo.getBaseUriBuilder().path(PlaylistResource.class).path(PlaylistResource.class, "getTags").build(playlist.getId()));
            if (getAuthUser().isDownload()) {
                representation.setDownloadUri(getAppURI(MyTunesRssCommand.GetZipArchive, "playlist=" + playlist.getId(), fn(playlist, "zip")));
            }
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
            representation.setImageUri(getAppURI(MyTunesRssCommand.ShowImage, "hash=" + track.getImageHash()));
        }
        if (getAuthUser().isPlaylist()) {
            representation.setM3uUri(getAppURI(MyTunesRssCommand.CreatePlaylist, "track=" + track.getId(), "type=M3u", fn(track, "m3u")));
            representation.setXspfUri(getAppURI(MyTunesRssCommand.CreatePlaylist, "track=" + track.getId(), "type=Xspf", fn(track, "xspf")));
        }
        if (getAuthUser().isRss()) {
            representation.setRssUri(getAppURI(MyTunesRssCommand.CreateRss, "track=" + track.getId(), fn(track, "rss")));
        }
        myRequest.setAttribute("downloadPlaybackServletUrl", MyTunesRssWebUtils.getServletUrl(myRequest)); // prepare MyTunesFunctions
        if (getAuthUser().isDownload()) {
            representation.setDownloadUri(UriBuilder.fromUri(MyTunesFunctions.downloadUrl(myRequest, track, null)).build());
        }
        if (MyTunesRssWebUtils.isHttpLiveStreaming(myRequest, track, true)) {
            representation.setPlaybackUri(UriBuilder.fromUri(MyTunesFunctions.playbackUrl(myRequest, track, null)).build());
        } else {
            representation.setPlaybackUri(UriBuilder.fromUri(MyTunesFunctions.httpLiveStreamUrl(myRequest, track, null)).build());
        }
        representation.setTagsUri(myUriInfo.getBaseUriBuilder().path(TrackResource.class).path(TrackResource.class, "getTags").build(track.getId()));
        representation.setArtistUri(myUriInfo.getBaseUriBuilder().path(ArtistResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(track.getArtist())));
        representation.setAlbumUri(myUriInfo.getBaseUriBuilder().path(AlbumResource.class).buildFromEncoded(MiscUtils.getUtf8UrlEncoded(track.getAlbumArtist()), MiscUtils.getUtf8UrlEncoded(track.getAlbum())));
        return representation;
    }

    public URI getAppURI(MyTunesRssCommand command, String... paths) {
        UriBuilder uriBuilder = UriBuilder.fromUri(MyTunesRssWebUtils.getCommandCall(myRequest, command));
        User authUser = getAuthUser();
        if (authUser != null) {
            uriBuilder.path(MyTunesRssWebUtils.encryptPathInfo(myRequest, "auth=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssBase64Utils.encode(authUser.getName()) + " " + MyTunesRssBase64Utils.encode(authUser.getPasswordHash()))));
        }
        for (String path : paths) {
            uriBuilder.path(path);
        }
        return uriBuilder.build();
    }

    protected String enc(String s) {
        return MyTunesRssWebUtils.encryptPathInfo(myRequest, s);
    }

    protected String b64(String s) {
        return MyTunesRssBase64Utils.encode(s);
    }

    protected String fn(Album album, String suffix) {
        return MyTunesFunctions.virtualAlbumName(album) + "." + suffix;
    }

    protected String fn(Artist artist, String suffix) {
        return MyTunesFunctions.virtualArtistName(artist) + "." + suffix;
    }

    protected String fn(Genre genre, String suffix) {
        return MyTunesFunctions.virtualGenreName(genre) + "." + suffix;
    }

    protected String fn(Track track, String suffix) {
        return MyTunesFunctions.virtualTrackName(track) + "." + suffix;
    }

    protected String fn(Playlist playlist, String suffix) {
        return MyTunesFunctions.webSafeFileName(playlist.getName()) + "." + suffix;
    }
}