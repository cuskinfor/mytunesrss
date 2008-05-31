package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Service for playlist retrieval and management.
 */
public class PlaylistService {
    // todo: remote-api: testing
    private static final Log LOG = LogFactory.getLog(PlaylistService.class);
    private static final String KEY_EDIT_PLAYLIST = "editPlaylist";
    private static final String KEY_EDIT_PLAYLIST_TRACKS = "editPlaylistTracks";

    /**
     * Get all visible playlist from the database.
     *
     * @return All visible playlists.
     *
     * @throws SQLException
     */
    public Object getPlaylists() throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, false, false);
            return RenderMachine.getInstance().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Get all visible playlists owned by the authorized user.
     *
     * @return All visible playlists owned by the authorized user.
     *
     * @throws SQLException
     */
    public Object getOwnPlaylists() throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, false, true);
            return RenderMachine.getInstance().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        throw new IllegalAccessException("Unauthorized");
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
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist,
                                                 "playlist=" + playlistId + "/type=" + StringUtils.capitalize(type.toLowerCase()));
    }

    /**
     * Get an URL for retrieving an RSS feed for the playlist with the specified ID.
     *
     * @param playlistId ID of the playlist.
     *
     * @return The URL for the RSS feed.
     */
    public String getRssUrl(String playlistId) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreateRss, "playlist=" + playlistId);
    }

    /**
     * Start editing a playlist. If an ID is specified it has be to the ID of a MyTunesRSS playlist.
     *
     * @param playlistId The ID of the playlist to edit or <code>null</code> to start an empty playlist.
     *
     * @return The playlist and track list.
     *
     * @throws IllegalAccessException   Unauthorized.
     * @throws SQLException             Could not execute SQL to find playlist.
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
     * Add a new track to the currently edited playlist.
     *
     * @param trackId The ID of the track to add.
     *
     * @return The playlist and list of tracks after adding the new track.
     *
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public Object addTrackToPlaylist(String trackId) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                if (playlist != null) {
                    List<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForId(new String[] {trackId}))
                            .getResults();
                    if (tracks != null && !tracks.isEmpty()) {
                        playlistTracks.add(tracks.get(0));
                        playlist.setTrackCount(playlistTracks.size());
                    }
                }
                return getPlaylistAndTracksRenderResult(playlist, playlistTracks);
            } else {
                throw new IllegalStateException("Not currently editing a playlist.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Add a number of tracks to the currently edited playlist.
     *
     * @param trackIds Array of track IDs of tracks to add.
     *
     * @return The playlist and list of tracks after adding the new tracks.
     *
     * @throws IllegalAccessException
     * @throws SQLException
     */
    public Object addTracksToPlaylist(String[] trackIds) throws IllegalAccessException, SQLException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                if (playlist != null) {
                    List<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForId(trackIds)).getResults();
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
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Remove a track from the currently edited playlist.
     *
     * @param trackId The ID of track to remove.
     *
     * @return The playlist and list of tracks after removing the track.
     *
     * @throws IllegalAccessException
     */
    public Object removeTrackFromPlaylist(String trackId) throws IllegalAccessException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                if (playlist != null) {
                    Track track = new Track();
                    track.setId(trackId);
                    playlistTracks.remove(track);
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
     * Remove a number of tracks from the currently edited playlist.
     *
     * @param trackIds The IDs of the tracks to remove.
     *
     * @return The playlist and list of tracks after removing the tracks.
     *
     * @throws IllegalAccessException
     */
    public Object removeTracksFromPlaylist(String[] trackIds) throws IllegalAccessException {
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

    public void savePlaylist(String playlistName, boolean userPrivate) throws IllegalAccessException {
        Session session = MyTunesRssRemoteEnv.getSession();
        User user = session.getUser();
        if (user != null) {
            Playlist playlist = (Playlist)session.getAttribute(KEY_EDIT_PLAYLIST);
            if (playlist != null) {
                Collection<Track> playlistTracks = (Collection<Track>)session.getAttribute(KEY_EDIT_PLAYLIST_TRACKS);
                playlist.setName(playlistName);
                playlist.setUserPrivate(userPrivate);
                playlist.setUserOwner(user.getName());
                // todo remote-api: save playlist
            } else {
                throw new IllegalStateException("Not currently editing a playlist.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

    public Object getEditPlaylist() throws IllegalAccessException {
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
