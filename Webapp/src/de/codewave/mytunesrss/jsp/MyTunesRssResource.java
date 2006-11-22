/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import javax.servlet.http.*;
import java.util.*;

public enum MyTunesRssResource {
    Login("/login.jsp"),
    Portal("/portal.jsp"),
    BrowseArtist("/browse_artist.jsp"),
    BrowseAlbum("/browse_album.jsp"),
    BrowseTrack("/browse_track.jsp"),
    PlaylistManager("/playlist_manager.jsp"),
    Settings("/settings.jsp"),
    EditPlaylist("/edit_playlist.jsp"),
    TemplateM3u("/m3u.jsp"),
    TemplateRss("/rss.jsp"),
    TrackInfo("/track_info.jsp"),
    FatalError("/fatal_error.jsp"),
    ShowUpload("/upload.jsp"),
    DatabaseUpdating("/database_updating.jsp");

    private String myValue;

    MyTunesRssResource(String value) {
        myValue = value;
    }

    public String getValue() {
        return myValue;
    }

    public void beforeForward(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Boolean> states = (Map<String, Boolean>)request.getSession().getAttribute("states");
        if (this != EditPlaylist && (states == null || !Boolean.TRUE.equals(states.get("addToPlaylistMode")))) {
            request.getSession().removeAttribute("playlist");
            request.getSession().removeAttribute("playlistContent");
        }
    }
}
