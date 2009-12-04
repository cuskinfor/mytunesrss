/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.UserAgent;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Map;

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
    TemplateXspf("/xspf.jsp"),
    TemplateQtPlugin("/qt_plugin.jsp"),
    TrackInfo("/track_info.jsp"),
    FatalError("/fatal_error.jsp"),
    ShowUpload("/upload.jsp"),
    BrowseServers("/browse_servers.jsp"),
    UploadProgress("/upload_progress.jsp"),
    BrowseGenre("/browse_genre.jsp"),
    Jukebox("/jukebox.jsp"),
    UploadFinished("/upload_finished.jsp"),
    RemoteControl("/remote_control.jsp"),
    EditSmartPlaylist("/edit_smart_playlist.jsp"),
    IphoneIndex("/iphone/index.jsp"),
    IphoneStartup("/iphone/startup.jsp"),
    OpenSearch("/opensearch.jsp");

    private String myValue;

    MyTunesRssResource(String value) {
        myValue = value;
    }

    public String getValue() {
        return myValue;
    }

    public void beforeForward(HttpServletRequest request, HttpServletResponse response) {
        Playlist playlist = (Playlist) MyTunesRssRemoteEnv.getSessionForRegularSession(request).getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
        if (playlist != null) {
            request.setAttribute("stateEditPlaylist", true);
            request.setAttribute("editPlaylistName", playlist.getName());
            request.setAttribute("editPlaylistTrackCount", playlist.getTrackCount());
        }
        if (this != BrowseServers) {
            request.getSession().removeAttribute("remoteServers");
        }
        if (this == Portal && !Boolean.TRUE.equals(request.getSession().getAttribute("welcomeMessageDone"))) {
            handleWelcomeMessage(request);
        }
        if (this == PlaylistManager) {
            request.setAttribute("deleteConfirmation", MyTunesRssWebUtils.getUserAgent(request) != UserAgent.Psp);
        }
        if (this == BrowseAlbum || this == BrowseArtist || this == BrowseGenre || this == BrowseTrack) {
            request.setAttribute("simpleNewPlaylist", MyTunesRssWebUtils.getUserAgent(request) == UserAgent.Psp);
        }
    }

    private void handleWelcomeMessage(HttpServletRequest request) {
        if (!Boolean.TRUE.equals(request.getSession().getAttribute("welcomeMessageDone"))) {
            String welcomeMessage = MyTunesRss.CONFIG.getWebWelcomeMessage();
            if (!StringUtils.isBlank(welcomeMessage)) {
                LocalizationContext context = (LocalizationContext)request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
                String message = welcomeMessage;
                if (context != null) {
                    try {
                        message = context.getResourceBundle().getString(welcomeMessage);
                    } catch (Exception e) {
                        // intentionally left blank
                    }
                }
                message = message.replace("'", "''").replace("{", "'{'");
                LocalizedError error = new LocalizedError(message);
                error.setEscapeXml(false);
                MyTunesRssWebUtils.addError(request, error, "messages");
            }
            request.getSession().setAttribute("welcomeMessageDone", Boolean.TRUE);
        }
    }
}
