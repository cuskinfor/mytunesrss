/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.datastore.statement.Artist;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.rest.representation.AlbumRepresentation;
import de.codewave.mytunesrss.rest.representation.ArtistRepresentation;
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

    protected List<ArtistRepresentation> toArtistRepresentation(List<Artist> artists) {
        List<ArtistRepresentation> representations = new ArrayList<ArtistRepresentation>();
        for (Artist artist : artists) {
            representations.add(toArtistRepresentation(artist));
        }
        return representations;
    }

    protected ArtistRepresentation toArtistRepresentation(Artist artist) {
        ArtistRepresentation representation = new ArtistRepresentation(artist);
        representation.getUri().put("albums", myUriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getAlbums").build(MyTunesRssBase64Utils.encode(artist.getName())));
        return representation;
    }

    protected List<AlbumRepresentation> toAlbumRepresentation(List<Album> albums) {
        List<AlbumRepresentation> representations = new ArrayList<AlbumRepresentation>();
        for (Album album : albums) {
            representations.add(toAlbumRepresentation(album));
        }
        return representations;
    }

    protected AlbumRepresentation toAlbumRepresentation(Album album) {
        AlbumRepresentation representation = new AlbumRepresentation(album);
        representation.getUri().put("tracks", myUriInfo.getBaseUriBuilder().path(ArtistResource.class).path(ArtistResource.class, "getArtistAlbumTracks").build(MyTunesRssBase64Utils.encode(album.getArtist()), MyTunesRssBase64Utils.encode(album.getName())));
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
