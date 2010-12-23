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
import java.text.SimpleDateFormat;
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
    UploadFinished("/upload_finished.jsp"),
    RemoteControl("/remote_control.jsp"),
    EditSmartPlaylist("/edit_smart_playlist.jsp"),
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
        if (this == Login) {
            handleLoginMessage(request);
        }
        if (this == Portal) {
            if (!Boolean.TRUE.equals(request.getSession().getAttribute("welcomeMessageDone"))) {
                handleWelcomeMessage(request);
            }
            long expiration = MyTunesRssWebUtils.getAuthUser(request).getExpiration();
            if (expiration > 0) {
                Error error = new BundleError("accountExpirationWarning", new SimpleDateFormat(MyTunesRssWebUtils.getBundleString(request, "dateFormat")).format(expiration));
                MyTunesRssWebUtils.addError(request, error, "messages");
            }
        }
        if (this == PlaylistManager) {
            request.setAttribute("deleteConfirmation", MyTunesRssWebUtils.getUserAgent(request) != UserAgent.Psp);
        }
        if (this == BrowseAlbum || this == BrowseArtist || this == BrowseGenre || this == BrowseTrack) {
            request.setAttribute("simpleNewPlaylist", MyTunesRssWebUtils.getUserAgent(request) == UserAgent.Psp);
        }
    }

    private void handleWelcomeMessage(HttpServletRequest request) {
        String welcomeMessage = MyTunesRss.CONFIG.getWebWelcomeMessage();
        handleMessage(request, welcomeMessage);
        request.getSession().setAttribute("welcomeMessageDone", Boolean.TRUE);
    }

    private void handleLoginMessage(HttpServletRequest request) {
        String loginMessage = MyTunesRss.CONFIG.getWebLoginMessage();
        handleMessage(request, loginMessage);
    }

    private void handleMessage(HttpServletRequest request, String welcomeMessage) {
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
    }
}
