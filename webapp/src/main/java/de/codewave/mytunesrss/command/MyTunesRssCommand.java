/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.utils.servlet.Command;
import de.codewave.utils.servlet.CommandHandler;

public enum MyTunesRssCommand implements Command {
    Login("login", DoLoginCommandHandler.class),
    ShowFatalError("showFatalError", ShowFatalErrorCommandHandler.class),
    ShowUploadProgress("showUploadProgress", ShowUploadProgressCommandHandler.class),
    Logout("logout", DoLogoutCommandHandler.class),
    ShowPortal("showPortal", ShowPortalCommandHandler.class),
    CheckHealth("checkHealth", CheckHealthCommandHandler.class),
    BrowseAlbum("browseAlbum", BrowseAlbumCommandHandler.class),
    BrowseArtist("browseArtist", BrowseArtistCommandHandler.class),
    BrowseTrack("browseTrack", BrowseTrackCommandHandler.class),
    StartNewPlaylist("startNewPlaylist", StartNewPlaylistCommandHandler.class),
    CancelCreatePlaylist("cancelCreatePlaylist", CancelCreatePlaylistCommandHandler.class),
    DeletePlaylist("deletePlaylist", DeletePlaylistCommandHandler.class),
    SavePlaylist("savePlaylist", SavePlaylistCommandHandler.class),
    ShowPlaylistManager("showPlaylistManager", ShowPlaylistManagerCommandHandler.class),
    CreatePlaylist("createPlaylist", CreatePlaylistCommandHandler.class),
    CreateRss("createRSS", CreateRssCommandHandler.class),
    ShowSettings("showSettings", ShowSettingsCommandHandler.class),
    SaveSettings("saveSettings", SaveSettingsCommandHandler.class),
    ShowTrackInfo("showTrackInfo", ShowTrackInfoCommandHandler.class),
    GetZipArchive("getZipArchive", GetZipArchiveCommandHandler.class),
    DownloadTrack("downloadTrack", DownloadTrackCommandHandler.class),
    PlayTrack("playTrack", PlayTrackCommandHandler.class),
    ShowUpload("showUpload", ShowUploadCommandHandler.class),
    BrowseServers("browseServers", BrowseServersCommandHandler.class),
    BrowseGenre("browseGenre", BrowseGenreCommandHandler.class),
    ShowImage("showImage", ShowImageCommandHandler.class),
    ShowJukebox("showJukebox", ShowJukeboxCommandHandler.class),
    ContinueExistingPlaylist("continueExistingPlaylist", ContinueExistingPlaylistCommandHandler.class),
    SendForgottenPassword("sendForgottenPassword", SendForgottenPasswordCommandHandler.class),
    CreateOneClickPlaylist("createOneClickPlaylist", CreateOneClickPlaylistCommandHandler.class),
    EditSmartPlaylist("editSmartPlaylist", EditSmartPlaylistCommandHandler.class),
    SaveSmartPlaylist("saveSmartPlaylist", SaveSmartPlaylistCommandHandler.class),
    YouTubeRedirect("youTubeRedirect", YouTubeRedirectCommandHandler.class),
    ShowRemoteControl("showRemoteControl", ShowRemoteControlHandler.class),
    SearchTracks("searchTracks", SearchTracksCommandHandler.class),
    ShowResource("showResource", ShowResourceCommandHandler.class),
    Upload("upload", UploadCommandHandler.class);

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
