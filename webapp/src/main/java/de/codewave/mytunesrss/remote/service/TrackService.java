package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * Service for track retrieval and management.
 */
public class TrackService {
    /**
     * Get an URL for downloading the track with the specified ID.
     *
     * @param trackId ID of the track.
     *
     * @return The URL for playback of the track.
     */
    public String getDownloadUrl(String trackId) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.DownloadTrack, "track=" + trackId);
    }
    /**
     * Get an URL for playing the track with the specified ID.
     *
     * @param trackId ID of the track.
     *
     * @return The URL for playback of the track.
     */
    public String getPlaybackUrl(String trackId) {
        return MyTunesRssRemoteEnv.getServerCall(MyTunesRssCommand.PlayTrack, "track=" + trackId);
    }
}