package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.command.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.xmlrpc.*;
import org.apache.commons.lang.*;

import java.sql.*;

/**
 * Service for playlist retrieval and management.
 */
public class PlaylistService {
    /**
     * Get all visible playlist from the database.
     *
     * @return All visible playlists.
     *
     * @throws SQLException
     */
    public Object getPlaylists() throws SQLException {
        FindPlaylistQuery query = new FindPlaylistQuery(MyTunesRssRemoteEnv.getUser(), null, null, false, false);
        return MyTunesRssRemoteEnv.getRenderMachine().render(TransactionFilter.getTransaction().executeQuery(query));
    }

    /**
     * Get all visible playlists owned by the authorized user.
     *
     * @return All visible playlists owned by the authorized user.
     *
     * @throws SQLException
     */
    public Object getOwnPlaylists() throws SQLException {
        FindPlaylistQuery query = new FindPlaylistQuery(MyTunesRssRemoteEnv.getUser(), null, null, false, true);
        return MyTunesRssRemoteEnv.getRenderMachine().render(TransactionFilter.getTransaction().executeQuery(query));
    }

    /**
     * Get an URL for retrieving the playlist with the specified ID.
     *
     * @param playlistId ID of the playlist.
     * @param type       Playlist type (M3u or Xspf).
     *
     * @return The URL for the specified playlist.
     */
    public String getPlaylistUrl(String playlistId, String type) {
        return MyTunesRssXmlRpcServlet.getServerCall(MyTunesRssCommand.CreatePlaylist, "playlist=" + playlistId + "/type=" + StringUtils.capitalize(
                type.toLowerCase()));
    }

    /**
     * Get an URL for retrieving an RSS feed for the playlist with the specified ID.
     *
     * @param playlistId ID of the playlist.
     *
     * @return The URL for the RSS feed.
     */
    public String getRssUrl(String playlistId) {
        return MyTunesRssXmlRpcServlet.getServerCall(MyTunesRssCommand.CreateRss, "playlist=" + playlistId);
    }
}
