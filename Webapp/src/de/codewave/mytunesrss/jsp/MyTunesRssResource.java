/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

public enum MyTunesRssResource {
    Login("/login.jsp"),
    Portal("/portal.jsp"),
    BrowseArtist("/browse_artist.jsp"),
    BrowseAlbum("/browse_album.jsp"),
    BrowseTrack("/browse_track.jsp"),
    PlaylistManager("/playlist_manager.jsp"),
    Settings("/settings.jsp"),
    TemplateM3u("/m3u.jsp"),
    TemplateRss("/rss.jsp");

    private String myValue;

    MyTunesRssResource(String value) {
        myValue = value;
    }

    public String getValue() {
        return myValue;
    }
}
