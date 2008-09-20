package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
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
     */
    public Object getPlaylists() throws SQLException, IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, null, false, false);
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
            FindPlaylistQuery query = new FindPlaylistQuery(user, null, null, null, false, true);
            return RenderMachine.getInstance().render(TransactionFilter.getTransaction().executeQuery(query));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Get an URL for retrieving the playlist with the specified ID.
     *
     * @param playlistId                  The playlist ID.
     * @param type                        The playlist type (M3u or Xspf).
     * @param alacTranscoding             <code>true</code> for ALAC transcoding or <code>false</code> for no ALAC transcoding.
     * @param faadTranscoding             <code>true</code> for AAC transcoding or <code>false</code> for no AAC transcoding.
     * @param lameTranscoding             <code>true</code> for MP3 transcoding or <code>false</code> for no MP3 transcoding.
     * @param transcodingBitrate          The target transcoding bit rate.
     * @param transcodingSamplerate       The target transcoding sample rate.
     * @param transcodeOnTheFlyIfPossible <code>true</code> to use transcoding on the fly if possible or <code>false</code> otherwise.
     *
     * @return The URL for the specified playlist.
     */
    public String getPlaylistUrl(String playlistId, String type, boolean alacTranscoding, boolean faadTranscoding, boolean lameTranscoding,
            int transcodingBitrate, int transcodingSamplerate, boolean transcodeOnTheFlyIfPossible) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreatePlaylist, "playlist=" + playlistId + "/type=" + StringUtils.capitalize(
                    type.toLowerCase()) + "/tc=" + MyTunesRssWebUtils.createTranscodingParamValue(alacTranscoding,
                                                                                                  faadTranscoding,
                                                                                                  lameTranscoding,
                                                                                                  transcodingBitrate,
                                                                                                  transcodingSamplerate,
                                                                                                  transcodeOnTheFlyIfPossible));
        }
        throw new IllegalAccessException("Unauthorized");
    }

    /**
     * Get an URL for retrieving an RSS feed for the playlist with the specified ID.
     *
     * @param playlistId                  The playlist ID.
     * @param alacTranscoding             <code>true</code> for ALAC transcoding or <code>false</code> for no ALAC transcoding.
     * @param faadTranscoding             <code>true</code> for AAC transcoding or <code>false</code> for no AAC transcoding.
     * @param lameTranscoding             <code>true</code> for MP3 transcoding or <code>false</code> for no MP3 transcoding.
     * @param transcodingBitrate          The target transcoding bit rate.
     * @param transcodingSamplerate       The target transcoding sample rate.
     * @param transcodeOnTheFlyIfPossible <code>true</code> to use transcoding on the fly if possible or <code>false</code> otherwise.
     *
     * @return The URL for the RSS feed.
     */
    public String getRssUrl(String playlistId, boolean alacTranscoding, boolean faadTranscoding, boolean lameTranscoding, int transcodingBitrate,
            int transcodingSamplerate, boolean transcodeOnTheFlyIfPossible) throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.CreateRss,
                                                     "playlist=" + playlistId + "/tc=" + MyTunesRssWebUtils.createTranscodingParamValue(
                                                             alacTranscoding,
                                                             faadTranscoding,
                                                             lameTranscoding,
                                                             transcodingBitrate,
                                                             transcodingSamplerate,
                                                             transcodeOnTheFlyIfPossible));
        }
        throw new IllegalAccessException("Unauthorized");
    }
}
