/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.utils.servlet.*;

public enum MyTunesRssCommand implements Command {
    Login("login", DoLoginCommandHandler.class),
    ShowPortal("showPortal", ShowPortalCommandHandler.class),
    CheckHealth("checkHealth", CheckHealthCommandHandler.class),
    BrowseAlbum("browseAlbum", BrowseAlbumCommandHandler.class),
    BrowseArtist("browseArtist", BrowseArtistCommandHandler.class),
    BrowseTrack("browseTrack", BrowseTrackCommandHandler.class),
    StartNewPlaylist("startNewPlaylist", StartNewPlaylistCommandHandler.class),
    CancelCreatePlaylist("cancelCreatePlaylist", CancelCreatePlaylistCommandHandler.class),
    AddToPlaylist("addToPlaylist", AddToPlaylistCommandHandler.class),
    EditPlaylist("editPlaylist", EditPlaylistCommandHandler.class),
    ShowPlaylistManager("showPlaylistManager", ShowPlaylistManagerCommandHandler.class),
    CreateM3u("createM3U", CreateM3uCommandHandler.class),
    CreateRss("createRSS", CreateRssCommandHandler.class),
    ShowSettings("showSettings", ShowSettingsCommandHandler.class),
    SaveSettings("saveSettings", SaveSettingsCommandHandler.class),
    PlayTrack("playTrack", PlayTrackCommandHandler.class);

    private String myName;
    private Class<? extends CommandHandler> myCommandHandlerClass;

    MyTunesRssCommand(String name, Class<? extends CommandHandler> commandHandlerClass) {
        myName = name;
        myCommandHandlerClass = commandHandlerClass;
    }

    public String getName() {
        return myName;
    }

    public Class<? extends CommandHandler> getCommandHandlerClass() {
        return myCommandHandlerClass;
    }
}
