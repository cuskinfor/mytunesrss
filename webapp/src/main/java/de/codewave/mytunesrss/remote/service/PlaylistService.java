package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

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
     * @throws IllegalAccessException
     */
    public Object getPlaylists() throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null && user.isPlaylist()) {
            FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, null, false, false);
            return RenderMachine.getInstance().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }

    /**
     * Get all visible playlists owned by the authorized user.
     *
     * @return All visible playlists owned by the authorized user.
     *
     * @throws SQLException
     * @throws IllegalAccessException
     */
    public Object getOwnPlaylists() throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null && user.isPlaylist()) {
            FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, null, false, true);
            return RenderMachine.getInstance().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }

    /**
     * Get the tracks of a playlist.
     *
     * @param playlistId ID of the playlist.
     * @param sortOrder  Sort order.
     *
     * @return List with the tracks of the playlist.
     *
     * @throws SQLException
     * @throws IllegalAccessException
     */
    public Object getTracks(String playlistId, String sortOrder) throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null && user.isPlaylist()) {
            SortOrder sortOrderEnum = null;
            if (StringUtils.isNotBlank(sortOrder)) {
                try {
                    sortOrderEnum = SortOrder.valueOf(sortOrder);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid sort order \"" + sortOrder + "\" specified.");
                }
            }
            return RenderMachine.getInstance().render(new QueryResultWrapper(TransactionFilter
                    .getTransaction().executeQuery(new FindPlaylistTracksQuery(user, playlistId, sortOrderEnum)), 0, -1));
        }
        throw new IllegalAccessException("UNAUTHORIZED");
    }
}
