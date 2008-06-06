package de.codewave.mytunesrss.remote.service;

import de.codewave.camel.mp3.Mp3Info;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;
import de.codewave.mytunesrss.servlet.TransactionFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

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

    public Object getTrackInfo(String trackId) throws IllegalAccessException, SQLException, IOException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user != null) {
            Collection<Track> tracks = TransactionFilter.getTransaction().executeQuery(FindTrackQuery.getForId(new String[] {trackId})).getResults();
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                Map<String, Object> result = (Map<String, Object>)RenderMachine.getInstance().render(track);
                if (FileSupportUtils.isMp3(track.getFile())) {
                    result.put("mp3info", Boolean.TRUE);
                    Mp3Info info = Mp3Utils.getMp3Info(new FileInputStream(track.getFile()));
                    result.put("avgBitRate", info.getAvgBitrate());
                    result.put("avgSampleRate", info.getAvgSampleRate());
                }
                return result;
            } else {
                throw new IllegalArgumentException("Track with ID \"" + trackId + "\" not found.");
            }
        }
        throw new IllegalAccessException("Unauthorized");
    }

}