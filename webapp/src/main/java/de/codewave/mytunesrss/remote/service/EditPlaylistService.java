package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Service for playlist retrieval and management.
 */
public class EditPlaylistService {
    // todo: remote-api: testing
    private static final Log LOG = LogFactory.getLog(PlaylistService.class);
    private static final String KEY_EDIT_PLAYLIST = "editPlaylist";
    private static final String KEY_EDIT_PLAYLIST_TRACKS = "editPlaylistTracks";

    /**
     * Start editing a playlist. If an ID is specified it has be to the ID of a MyTunesRSS playlist.
     *
     * @param playlistId The ID of the playlist to edit or <code>null</code> to start an empty playlist.
     *
     * @return The playlist and track list.
     *
     * @throws IllegalAccessException   Unauthorized.
     * @throws java.sql.SQLException    Could not execute SQL to find playlist.
     * @throws IllegalArgumentException Playlist with specified ID not found.
     */
    public Object startEditPlaylist(String playlistId) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = new Playlist();
            Collection<Track> tracks = new ArrayList<Track>();
            if (StringUtils.isNotEmpty(playlistId)) {
                FindPlaylistQuery query = new FindPlaylistQuery(user, PlaylistType.MyTunes, playlistId, true, true);
                List<Playlist> queryResult = TransactionFilter.getTransaction().executeQuery(query).getResults();
                if (queryResult != null && queryResult.size() == 1) {
                    playlist = queryResult.get(0);
                    tracks = new LinkedHashSet<Track>(TransactionFilter.getTransaction().executeQuery(new FindPlaylistTracksQuery(user,
                                                                                                                                  playlistId,
                                                                                                                                  null)).getResults());
                } else {
                    throw new IllegalArgumentException("Playlist not found");
                }
            }
            session.setAttribute(KEY_EDIT_PLAYLIST, playlist);
            session.setAttribute(KEY_EDIT_PLAYLIST_TRACKS, tracks);
            return getPlaylistAndTracksRenderResult(playlist, tracks);
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Create the render result for a playlist and the playlist tracks.
     *
     * @param playlist The playlist.
     * @param tracks   The tracks of the playlist.
     *
     * @return The render result.
     */
    private Object getPlaylistAndTracksRenderResult(Playlist playlist, Collection<Track> tracks) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("playlist", playlist);
        map.put("tracks", tracks);
        return RenderMachine.getInstance().render(map);
    }

    /**
     * Add a number of tracks to the currently edited playlist.
     *
     * @param trackIds Array of track IDs of tracks to add.
     *
     * @return The playlist and list of tracks after adding the new tracks.
     *
     * @throws IllegalAccessException
     * @throws java.sql.SQLException
     */
    public Object addTracks(String[] trackIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return addTracks(FindTrackQuery.getForId(trackIds));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Add all tracks returned from the specified query.
     *
     * @param query The query which must return a collection of tracks.
     *
     * @return The render result.
     *
     * @throws SQLException
     * @throws IllegalAccessException
     */
    private Object addTracks(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException, IllegalAccessException {
        Session session = MyTunesRssRemoteEnv.getSession();
        Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
        if (playlist != null) {
            Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
            if (playlist != null) {
                List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
                if (tracks != null && !tracks.isEmpty()) {
                    playlistTracks.addAll(tracks);
                    playlist.setTrackCount(playlistTracks.size());
                }
            }
            return getPlaylistAndTracksRenderResult(playlist, playlistTracks);
        } else {
            throw new IllegalStateException("Not currently editing a playlist.");
        }
    }

    /**
     * Add all tracks from the specified albums to the currently edited playist.
     *
     * @param albumIds IDs of the albums to add.
     *
     * @return The playlist and list of tracks after adding the new tracks from the albums.
     *
     * @throws IllegalAccessException
     */
    public Object addAlbums(String[] albumIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return addTracks(FindTrackQuery.getForAlbum(user, albumIds, false));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Add all tracks from the specified artists to the currently edited playist.
     *
     * @param artistIds IDs of the artists to add.
     *
     * @return The playlist and list of tracks after adding the new tracks from the artists.
     *
     * @throws IllegalAccessException
     */
    public Object addArtists(String[] artistIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return addTracks(FindTrackQuery.getForArtist(user, artistIds, false));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Add all tracks from the specified genres to the currently edited playist.
     *
     * @param genreIds IDs of the genres to add.
     *
     * @return The playlist and list of tracks after adding the new tracks from the genres.
     *
     * @throws IllegalAccessException
     */
    public Object addGenres(String[] genreIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return addTracks(FindTrackQuery.getForGenre(user, genreIds, false));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Remove a number of tracks from the currently edited playlist.
     *
     * @param trackIds The IDs of the tracks to remove.
     *
     * @return The playlist and list of tracks after removing the tracks.
     *
     * @throws IllegalAccessException
     */
    public Object removeTracks(String[] trackIds) throws IllegalAccessException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                if (playlist != null) {
                    for (int i = 0; i < trackIds.length; i++) {
                        Track track = new Track();
                        track.setId(trackIds[i]);
                        playlistTracks.remove(track);
                    }
                    playlist.setTrackCount(playlistTracks.size());
                }
                return getPlaylistAndTracksRenderResult(playlist, playlistTracks);
            } else {
                throw new IllegalStateException("Not currently editing a playlist.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Remove all tracks of the specified albums from the currently edited playist.
     *
     * @param albumIds IDs of the albums to remove.
     *
     * @return The playlist and list of tracks after removing the tracks of the albums.
     *
     * @throws IllegalAccessException
     */
    public Object removeAlbums(String[] albumIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return removeTracks(FindTrackQuery.getForAlbum(user, albumIds, false));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Remove all tracks of the specified artists from the currently edited playist.
     *
     * @param artistIds IDs of the artists to remove.
     *
     * @return The playlist and list of tracks after re,moving the tracks of the artists.
     *
     * @throws IllegalAccessException
     */
    public Object removeArtists(String[] artistIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return removeTracks(FindTrackQuery.getForArtist(user, artistIds, false));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Remove all tracks of the specified genres from the currently edited playist.
     *
     * @param genreIds IDs of the genres to remove.
     *
     * @return The playlist and list of tracks after removing the tracks of the genres.
     *
     * @throws IllegalAccessException
     */
    public Object removeGenres(String[] genreIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            return removeTracks(FindTrackQuery.getForGenre(user, genreIds, false));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Remove the tracks returned from the specified query.
     *
     * @param query The query which must return a collection of tracks.
     *
     * @return The render result.
     *
     * @throws IllegalAccessException
     */
    private Object removeTracks(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
        if (playlist != null) {
            Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
            if (playlist != null) {
                List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
                if (tracks != null && !tracks.isEmpty()) {
                    for (Track track : tracks) {
                        playlistTracks.remove(track);
                    }
                    playlist.setTrackCount(playlistTracks.size());
                }
            }
            return getPlaylistAndTracksRenderResult(playlist, playlistTracks);
        } else {
            throw new IllegalStateException("Not currently editing a playlist.");
        }

    }

    /**
     * Cancel editing the playlist.
     *
     * @throws IllegalAccessException
     */
    public void cancelEditPlaylist() throws IllegalAccessException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                session.removeAttribute(KEY_EDIT_PLAYLIST);
                session.removeAttribute(KEY_EDIT_PLAYLIST_TRACKS);
            } else {
                throw new IllegalStateException("Not currently editing a playlist.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Save the currently edited playlist.
     *
     * @param playlistName Name of the playlist.
     * @param userPrivate  <code>true</code> for a private playlist or <code>false</code> for a public playlist.
     *
     * @throws IllegalAccessException
     */
    public void savePlaylist(String playlistName, boolean userPrivate) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                playlist.setName(playlistName);
                playlist.setUserPrivate(userPrivate);
                playlist.setUserOwner(user.getName());
                SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(user.getName(), userPrivate);
                statement.setId(playlist.getId());
                statement.setName(StringUtils.isEmpty(playlistName) ? playlist.getName() : playlistName);
                statement.setUpdate(StringUtils.isNotEmpty(playlist.getId()));
                List<String> trackIds = new ArrayList<String>(playlistTracks.size());
                for (Track track : playlistTracks) {
                    trackIds.add(track.getId());
                }
                statement.setTrackIds(trackIds);
                TransactionFilter.getTransaction().executeStatement(statement);
                session.removeAttribute(KEY_EDIT_PLAYLIST);
                session.removeAttribute(KEY_EDIT_PLAYLIST_TRACKS);
            } else {
                throw new IllegalStateException("Not currently editing a playlist.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Get the playlist and the list of tracks.
     *
     * @return The playlist and the list of tracks.
     *
     * @throws IllegalAccessException
     */
    public Object getPlaylist() throws IllegalAccessException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                return getPlaylistAndTracksRenderResult(playlist, playlistTracks);
            } else {
                throw new IllegalStateException("Not currently editing a playlist.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }
}