/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.config.User;

public enum UserPermission {

    Audio(), Video(), Rss(), Playlist(), Download(), YahooPlayer(), SpecialPlaylists(), Player(), RemoteControl(), ExternalSites(),
    Trascoder(), ChangePassword(), changeEmail(), EditLastFmAccount(), EditWebSettings(), CreatePlaylists(), CreatePublicPlaylists(), Photos(),
    DownloadPhotoAlbum(), Share();

    public boolean isGranted(User user) {
        switch (this) {
           case Audio:
                return user.isAudio();
            case Video:
                return user.isVideo();
            case Rss:
                return user.isRss();
            case Playlist:
                return user.isPlaylist();
            case Download:
                return user.isDownload();
            case YahooPlayer:
                return user.isYahooPlayer();
            case SpecialPlaylists:
                return user.isSpecialPlaylists();
            case Player:
                return user.isPlayer();
            case RemoteControl:
                return user.isRemoteControl();
            case ExternalSites:
                return user.isExternalSites();
            case Trascoder:
                return user.isTranscoder();
            case ChangePassword:
                return user.isChangePassword();
            case changeEmail:
                return user.isChangeEmail();
            case EditLastFmAccount:
                return user.isEditLastFmAccount();
            case EditWebSettings:
                return user.isEditWebSettings();
            case CreatePlaylists:
                return user.isCreatePlaylists();
            case CreatePublicPlaylists:
                return user.isCreatePublicPlaylists();
            case Photos:
                return user.isPhotos();
            case DownloadPhotoAlbum:
                return user.isDownloadPhotoAlbum();
            case Share:
                return user.isShare();
            default:
                throw new IllegalArgumentException("Unexpected user permission \"" + this + "\".");
        }
    }

}
