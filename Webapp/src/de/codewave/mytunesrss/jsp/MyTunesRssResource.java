/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.*;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import javax.servlet.jsp.jstl.core.*;
import javax.servlet.jsp.jstl.fmt.*;
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
    TemplateXspf("/xspf.jsp"),
    TrackInfo("/track_info.jsp"),
    FatalError("/fatal_error.jsp"),
    ShowUpload("/upload.jsp"),
    BrowseServers("/browse_servers.jsp"),
    DatabaseUpdating("/database_updating.jsp"),
    UploadFinished("/upload_finished.jsp"),
    UploadProgress("/upload_progress.jsp"),
    BrowseGenre("/browse_genre.jsp");

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
        if (this != BrowseServers) {
            request.getSession().removeAttribute("remoteServers");
        }
        if (this == Portal && !Boolean.TRUE.equals(request.getSession().getAttribute("welcomeMessageDone"))) {
            handleWelcomeMessage(request);
        }
    }

    private void handleWelcomeMessage(HttpServletRequest request) {
        String welcomeMessage = MyTunesRss.CONFIG.getWebWelcomeMessage();
        if (!StringUtils.isBlank(welcomeMessage)) {
            LocalizationContext context = (LocalizationContext)request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
            if (context != null) {
                try {
                    request.setAttribute("welcomeMessage", context.getResourceBundle().getString(welcomeMessage));
                } catch (Exception e) {
                    request.setAttribute("welcomeMessage", welcomeMessage);
                }
            } else {
                request.setAttribute("welcomeMessage", welcomeMessage);
            }
        }
        request.getSession().setAttribute("welcomeMessageDone", Boolean.TRUE);
    }
}
